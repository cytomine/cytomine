package be.cytomine.config.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class IncomingAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IncomingAuthorizationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            authorization = request.getParameter("authorization");
        }
        log.warn("DIAG capture thread={} uri={} authPresent={}",
            Thread.currentThread().getName(), request.getRequestURI(), authorization != null);
        IncomingAuthorizationContext.set(authorization);
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.warn("DIAG clear thread={} uri={}", Thread.currentThread().getName(), request.getRequestURI());
            IncomingAuthorizationContext.clear();
        }
    }
}
