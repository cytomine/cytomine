package org.cytomine.common.security;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import static java.util.function.Predicate.not;

public class IncomingAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        IncomingAuthorizationContext.set(new IncomingAuthorizationContext.Headers(
            resolveAuthorization(request),
            request.getHeader("date"),
            request.getHeader("content-MD5"),
            request.getHeader("Content-Type")
        ));
        try {
            filterChain.doFilter(request, response);
        } finally {
            IncomingAuthorizationContext.clear();
        }
    }

    private String resolveAuthorization(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            return authorization;
        }
        return Optional.ofNullable(request.getParameter("auth"))
            .filter(not(String::isBlank))
            .or(() -> Optional.ofNullable(request.getParameter("authorization")).filter(not(String::isBlank)))
            .or(() -> Optional.ofNullable(request.getParameter("Authorization")).filter(not(String::isBlank)))
            .orElse(null);
    }
}
