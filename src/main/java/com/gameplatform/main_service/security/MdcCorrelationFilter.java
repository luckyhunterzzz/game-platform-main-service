package com.gameplatform.main_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filter for request tracing.
 * It extracts the Correlation ID from the gateway and puts it into the MDC (Mapped Diagnostic Context)
 */
public class MdcCorrelationFilter extends OncePerRequestFilter {
    private static final String CORRELATION_ID_HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";

    /**
     * Reads X-Request-Id header and adds it to the logging context.
     * This allows to see the same ID for one request across all logs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(CORRELATION_ID_HEADER);
        if (requestId != null) {
            MDC.put(MDC_KEY, requestId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
