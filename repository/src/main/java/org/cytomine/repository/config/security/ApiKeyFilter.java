package org.cytomine.repository.config.security;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.persistence.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import be.cytomine.common.config.security.CytomineAuthenticationSupport;

public class ApiKeyFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final UserRepository userRepository;

    public ApiKeyFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        tryApiKeyAuthentication(request);
        filterChain.doFilter(request, response);
    }

    private boolean tryApiKeyAuthentication(HttpServletRequest request) {
        String authorization = request.getHeader("authorization");
        if (authorization == null) {
            return false;
        }
        Optional<CytomineAuthenticationSupport.Credentials> credentials =
            CytomineAuthenticationSupport.parse(authorization);
        if (credentials.isEmpty()) {
            return false;
        }
        try {
            String contentMd5 = Objects.requireNonNullElse(request.getHeader("content-MD5"), "");
            String contentType = Objects.requireNonNullElse(request.getHeader("Content-Type"),
                Objects.requireNonNullElse(request.getHeader("content-type"), ""));
            String date = Objects.requireNonNullElse(request.getHeader("date"), "");
            String accessKey = credentials.get().accessKey();

            Optional<UserEntity> user = userRepository.findByPublicKeyAndEnabled(accessKey, true);

            if (user.isEmpty()) {
                log.debug("User cannot be extracted with accessKey {}", accessKey);
                return false;
            } else if (CytomineAuthenticationSupport.matchesSignature(
                request.getMethod(), contentMd5, contentType, date,
                user.get().getPrivateKey(), credentials.get().signature())
            ) {
                this.authenticate(user.get());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.debug("Cannot authenticate with the CYTOMINE authorization scheme", e);
            return false;
        }
    }

    private void authenticate(UserEntity user) {
        UserDetails userDetails = createSpringSecurityUser(user);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(UserEntity user) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), "null",
            user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList()));
    }
}
