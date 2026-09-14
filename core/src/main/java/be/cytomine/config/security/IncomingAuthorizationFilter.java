package be.cytomine.config.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class IncomingAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            authorization = request.getParameter("authorization");
        }
        IncomingAuthorizationContext.set(new IncomingAuthorizationContext.Headers(
            authorization,
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
}
