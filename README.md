# api-idempotency-spring-boot-backend

API REST que implementa un mecanismo de **idempotencia** para garantizar
que solicitudes duplicadas (reintentos de red, doble clic del cliente,
timeouts) no generen efectos repetidos en operaciones críticas — en este
caso, procesar un pago.

Arquitectura hexagonal (Ports & Adapters) · Java 21 · Spring Boot 3.3.2

## Stack

| Componente | Uso en este proyecto |
|---|---|
| **PostgreSQL** | Persistencia real de la entidad de negocio (`Payment`) |
| **Redis** | Almacén del candado/registro de idempotencia (único backend: no hay tabla de idempotencia en Postgres) |
| **HandlerInterceptor** | Único mecanismo de idempotencia implementado (no hay `@Aspect`/AOP activo, ver sección "Estado actual") |
| **Docker / Docker Compose** | Levanta Postgres, Redis y la propia app juntos |

## 1. Estructura del proyecto (arquitectura hexagonal)

```
src/main/java/com/jesus/portafolio/idempotency/
│
├── domain/model/                    <-- núcleo de negocio, sin Spring
│   ├── Payment.java
│   └── PaymentStatus.java
│
├── application/                     <-- casos de uso y puertos
│   ├── annotation/
│   │   └── Idempotency.java             (marca los endpoints protegidos)
│   ├── model/
│   │   └── IdempotentRecord.java        (no usado actualmente, ver notas)
│   ├── port/
│   │   ├── in/ProcessPaymentUseCase.java
│   │   └── out/
│   │       ├── IdempotencyStoragePort.java
│   │       ├── IdempotencyDecision.java, IdempotentPayload.java
│   │       └── PaymentRepositoryPort.java
│   └── service/
│       └── ProcessPaymentServiceInterceptor.java
│
└── infrastructure/
    ├── adapter/
    │   ├── in/web/
    │   │   ├── controller/InterceptorPaymentController.java
    │   │   ├── dto/PaymentRequest.java, PaymentResponse.java
    │   │   ├── mapper/PaymentWebMapper.java
    │   │   ├── interceptor/IdempotencyInterceptor.java
    │   │   ├── filter/CachingFilter.java
    │   │   └── exception/GlobalExceptionHandler.java, MissingIdempotencyKeyException.java
    │   └── out/
    │       ├── redis/
    │       │   ├── RedisIdempotencyStorageAdapter.java   <-- implementa IdempotencyStoragePort
    │       │   └── RedisIdempotencyRecord.java
    │       ├── persistence/postgresql/
    │       │   ├── PaymentJpaEntity.java, PaymentJpaRepository.java
    │       │   └── JpaPaymentRepositoryAdapter.java       <-- implementa PaymentRepositoryPort
    │       └── mapper/PaymentMapper.java
    └── config/
        ├── WebMvcConfig.java   (registra el interceptor)
        └── RedisConfig.java    (placeholder, sin beans propios -- Spring Boot autoconfigura Redis)
```

`domain` y `application` no dependen de Spring Web, JPA ni Redis — todo
eso vive en `infrastructure`, detrás de los puertos `IdempotencyStoragePort`
y `PaymentRepositoryPort`.

## 2. Cómo funciona la idempotencia en este proyecto

1. El cliente envía la cabecera **`x-idempotency-key`** (un UUID generado
   por él) en el `POST /api/v1/payment`.
2. `CachingFilter` envuelve el request/response para poder leer el body
   más de una vez (una vez para calcular el hash, otra para que el
   controller lo deserialice).
3. `IdempotencyInterceptor` (HandlerInterceptor):
    - Verifica que el método tenga la anotación `@Idempotency`.
    - Exige la cabecera `x-idempotency-key` (si falta -> `400 Bad Request`, `MISSING_IDEMPOTENCY_KEY`).
    - Calcula un hash SHA-256 de `método + path + body`.
    - Le pregunta a `IdempotencyStoragePort.begin(...)` si ya se vio esa clave.
4. `RedisIdempotencyStorageAdapter` resuelve `begin` con un `SET NX EX`
   atómico sobre Redis (`SETNX` + TTL): si la clave no existía, la reserva
   y dice "procede"; si ya existía y está `COMPLETED` con el mismo hash,
   devuelve la respuesta cacheada; si está `IN_PROGRESS`, hay una colisión
   de concurrencia.
5. Si es la primera vez, el controller se ejecuta normalmente y, al
   terminar, `afterCompletion` llama a `complete(...)`, que guarda en
   Redis el status/body/content-type de la respuesta con el mismo TTL.
6. Un reintento con la misma clave nunca vuelve a ejecutar el controller:
   `IdempotencyInterceptor` corta la cadena (`return false`) y responde
   directamente con lo guardado, agregando la cabecera
   `Idempotent-Replayed: true`.

**Nota de diseño**: a diferencia de otras variantes de este mismo
proyecto, aquí **Redis es la única fuente de verdad del candado de
idempotencia** (no hay tabla de idempotencia en Postgres respaldando el
candado). Postgres solo persiste la entidad `Payment` en sí. Esto es más
simple, pero implica que si Redis pierde datos (no tiene volumen de
persistencia en el `docker-compose`), se pierde también la protección
contra duplicados para las claves activas en ese momento.

## 3. Endpoints

| Método/Ruta | Protegido | Descripción |
|---|---|---|
| `POST /api/v1/payment` | Sí (`@Idempotency`, TTL 86400s) | Crea un pago; reintentos con la misma clave devuelven la misma respuesta |
| `POST /api/v1/payment/unsafe` | No | Mismo caso de uso, sin protección — para contrastar |
| `GET /api/v1/payment/executions-count` | — | Cuenta cuántos pagos se procesaron de verdad en Postgres |

### Ejemplo con curl

```bash
# Primera vez: se procesa el pago de verdad
curl -i -X POST http://localhost:8082/api/v1/payment \
  -H "Content-Type: application/json" \
  -H "x-idempotency-key: 11111111-1111-1111-1111-111111111111" \
  -d '{"accountId": "acc-1", "amout": 50.0, "currency": "USD"}'

# Reintento con la MISMA clave -> respuesta cacheada (Idempotent-Replayed: true)
curl -i -X POST http://localhost:8082/api/v1/payment \
  -H "Content-Type: application/json" \
  -H "x-idempotency-key: 11111111-1111-1111-1111-111111111111" \
  -d '{"accountId": "acc-1", "amout": 50.0, "currency": "USD"}'

curl http://localhost:8082/api/v1/payment/executions-count
```

> Nota: el campo del body es `amout` (no `amount`) porque así está
> declarado hoy en `PaymentRequest` — ver sección "Estado actual y
> mejoras pendientes".

## 4. Docker y Docker Compose

### Dockerfile (multi-stage)

```dockerfile
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Etapa 1 (build)**: compila con Maven sobre JDK 21, descargando
  dependencias en una capa separada (`dependency:go-offline`) para
  aprovechar la caché de Docker en builds sucesivos.
- **Etapa 2 (runtime)**: imagen final ligera (`jre-alpine`, sin JDK
  completo), corre como usuario no-root (`appuser`), expone el puerto
  `8082` (el mismo que usa `application.yml`).

### docker-compose.yml (Postgres + Redis + la app, todo junto)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: idempotency-postgres
    environment:
      POSTGRES_DB: idempotency_demo
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - idempotency-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d idempotency_demo"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - idempotency-net

  redis:
    image: redis:7-alpine
    container_name: idempotency-redis
    ports:
      - "6379:6379"
    # Sin volumen de persistencia a propósito: aquí Redis actúa como el
    # backend del candado de idempotencia -- ver nota de diseño arriba
    # sobre el trade-off de no respaldarlo también en Postgres.
    command: ["redis-server", "--save", "", "--appendonly", "no"]
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - idempotency-net

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: idempotency-app
    ports:
      - "8082:8082"
    environment:
      # Sobreescriben application.yml (que usa "localhost", pensado para
      # correr la app fuera de Docker). Dentro de la red de docker-compose,
      # los contenedores se resuelven por nombre de servicio.
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/idempotency_demo
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - idempotency-net

networks:
  idempotency-net:
    driver: bridge

volumes:
  idempotency-postgres-data:
```

| Variable de entorno (servicio `app`) | Sobreescribe en `application.yml` |
|---|---|
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` |
| `SPRING_DATA_REDIS_HOST` | `spring.data.redis.host` |
| `SPRING_DATA_REDIS_PORT` | `spring.data.redis.port` |

Spring Boot mapea automáticamente estas variables (relaxed binding) sobre
las propiedades del YAML, sin tener que tocar el archivo ni mantener dos
versiones de configuración.

## 5. Cómo correrlo

### Todo junto (app + Postgres + Redis) con Docker

```bash
docker compose up -d --build
docker compose ps        # confirma que los 3 servicios estén healthy/running
docker compose logs -f app
```

La app queda en `http://localhost:8082`.

### Solo la infraestructura, corriendo la app desde el IDE/Maven

```bash
docker compose up -d postgres redis
mvn spring-boot:run
```

En este caso `application.yml` ya apunta a `localhost:5432` y
`localhost:6379`, que es justo lo que expone el `docker-compose` en el
host.

### Apagar todo

```bash
docker compose down          # conserva el volumen de Postgres
docker compose down -v       # borra también el volumen (datos de Postgres)
```

## 6. Estado actual y mejoras pendientes

Notas "próximos pasos":

- **Manejo de errores incompleto**: `RedisIdempotencyStorageAdapter`
  lanza `RuntimeException` genérica para los casos "solicitud en curso" y
  "clave reutilizada con otro payload" (hay un comentario `// Cambiar por
  una exception que extienda de RuntimeException` en el propio código).
  Hoy eso se traduce en un `500 Internal Server Error` sin cuerpo
  estructurado, en vez de un `409 Conflict` o `422 Unprocessable Entity`
  con el formato que sí tiene `MissingIdempotencyKeyException` vía
  `GlobalExceptionHandler`.
- **`spring-boot-starter-aop`** Implementar `@aspect` como ejemplo de idempotencia.
