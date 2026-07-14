package com.jesus.portafolio.idempotency.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request);

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        String body = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        request.setAttribute("cachedBody", body);

        responseWrapper.copyBodyToResponse();
    }
}