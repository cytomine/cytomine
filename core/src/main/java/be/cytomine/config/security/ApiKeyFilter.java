package be.cytomine.config.security;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import be.cytomine.common.config.security.CytomineAuthenticationSupport;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AuthenticationException;
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.UserRepository;

@Deprecated
public class ApiKeyFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final UserRepository secUserRepository;

    private final UserMapper userMapper;

    public ApiKeyFilter(UserRepository secUserRepository, UserMapper userMapper) {
        this.secUserRepository = secUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        tryAPIAuthentification(request);
        filterChain.doFilter(request, response);
    }

    private boolean tryAPIAuthentification(HttpServletRequest request) {
        // http://code.google.com/apis/storage/docs/reference/v1/developer-guidev1.html#authentication
        if (request.getHeader("date") == null) {
            return false;
        }
        if (request.getHeader("host") == null) {
            return false;
        }
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
            String date = (request.getHeader("date") != null) ? request.getHeader("date") : "";
            String accessKey = credentials.get().accessKey();

            Optional<User> user = secUserRepository.findByPublicKeyAndEnabled(accessKey, true);

            if (user.isEmpty()) {
                log.debug("User cannot be extracted with accessKey {}", accessKey);
                throw new AuthenticationException("User cannot be extracted with accessKey " + accessKey);
            } else if (CytomineAuthenticationSupport.matchesSignature(
                request.getMethod(), contentMd5, contentType, date,
                user.get().getPrivateKey(), credentials.get().signature())
            ) {
                this.reauthenticate(user.get());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Rebuild an Authentication for the given username and register it in the security context. Typically used after
     * updating a user's authorities or other auth-cached info.
     * <p/>
     * Also removes the user from the user cache to force a refresh at next login.
     */
    private void reauthenticate(final User secUser) {
        UserDetails userDetails = createSpringSecurityUser(secUser);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());
        authenticationToken.setDetails(userMapper.map(secUser));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(User user) {
        if (!user.getEnabled()) {
            throw new ForbiddenException("User with access key " + user.getPublicKey() + "is not enabled.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), "null",
            user.getRoles().stream().map(x -> new SimpleGrantedAuthority(x.getAuthority()))
                .collect(Collectors.toList()));
    }
}
