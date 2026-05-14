package io.divtrack.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        long start = System.nanoTime();
        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            log.error("request failed method={} path={} error={}", request.getMethod(), getPath(request), e.getMessage());
            throw new RuntimeException(e);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;

            MDC.put("method", request.getMethod());
            MDC.put("path", getPath(request));
            MDC.put("status", String.valueOf(wrappedResponse.getStatus()));
            MDC.put("duration_ms", String.valueOf(ms));
            if (request.getQueryString() != null) MDC.put("query", request.getQueryString());

            String body = getResponseBody(wrappedResponse);
            if (body != null) MDC.put("response_body", body);

            log.info("{} {} {} ({}ms)", request.getMethod(), getPath(request), wrappedResponse.getStatus(), ms);

            try { wrappedResponse.copyBodyToResponse(); } catch (Exception ignored) {}
            MDC.clear();
        }
    }

    private String getPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0 || content.length > MAX_PAYLOAD_LENGTH) return null;
        return new String(content, StandardCharsets.UTF_8);
    }
}
