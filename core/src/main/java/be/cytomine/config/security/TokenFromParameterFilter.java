package be.cytomine.config.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import static java.util.function.Predicate.not;

public class TokenFromParameterFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        Optional<String> authToken =
            Optional.ofNullable(request.getParameter("auth"))
                .filter(not(String::isBlank))
                .or(() -> Optional.ofNullable(request.getParameter("Authorization"))
                              .filter(not(String::isBlank)))
                .map(token -> token.startsWith("Bearer") ? token : "Bearer " + token);

        if (authToken.isPresent() && request.getHeader("Authorization") == null) {
            // Wrap the request to "fake" the Authorization header
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return authToken.get();
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    if (!names.contains("Authorization")) {
                        names.add("Authorization");
                    }
                    return Collections.enumeration(names);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
