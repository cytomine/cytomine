package be.cytomine.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.ServerException;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.security.current.CurrentUser;
import be.cytomine.security.current.FullCurrentUser;
import be.cytomine.security.current.PartialCurrentUser;

// TODO IAM: adapt to get the Cytomine user from IAM reference
@Slf4j
@RequiredArgsConstructor
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final UserHttpContract userHttpContract;

    public String getCurrentUsername() {
        CurrentUser currentUser = getSecurityCurrentUser().orElseThrow(() -> new ServerException(
            "Cannot read current user"));
        if (currentUser.isFullObjectProvided() || currentUser.isUsernameProvided()) {
            return currentUser.getUser().username();
        } else {
            throw new ObjectNotFoundException(
                "User",
                "Cannot read current username. Object " + currentUser + " is not supported"
            );
        }
    }

    public UserResponse getCurrentUser() {
        CurrentUser currentUser = getSecurityCurrentUser().orElseThrow(() -> new ServerException(
            "Cannot read current user"));
        UserResponse user;
        if (currentUser.isFullObjectProvided()) {
            user = currentUser.getUser();
        } else if (currentUser.isUsernameProvided()) {
            user = userHttpContract.search(currentUser.getUser().username())
                .orElseThrow(() -> new ServerException("Cannot find current user with username " + currentUser.getUser()
                    .username()));
        } else {
            throw new ObjectNotFoundException(
                "User",
                "Cannot read current user. Object " + currentUser + " is not supported"
            );
        }
        return user;
    }

    public User getCurrentUserOld(){
        return userRepository.findById(getCurrentUser().id()).orElseThrow();
    }

    public UserResponse getCurrentUser(String username) {
        return userHttpContract.search(username)
            .orElseThrow(() -> new ServerException("Cannot find current user with username " + username));
    }

    public static Optional<CurrentUser> getSecurityCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractCurrentUser(securityContext.getAuthentication()));
    }

    private static CurrentUser extractCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getDetails() instanceof User) {
            FullCurrentUser fullCurrentUser = new FullCurrentUser();
            fullCurrentUser.setUser((User) authentication.getDetails());
            return fullCurrentUser;
        } else if (authentication.getPrincipal() instanceof String) {
            PartialCurrentUser partialCurrentUser = new PartialCurrentUser();
            partialCurrentUser.setUsername((String) authentication.getPrincipal());
            return partialCurrentUser;
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            PartialCurrentUser partialCurrentUser = new PartialCurrentUser();
            partialCurrentUser.setUsername(((UserDetails) authentication.getPrincipal()).getUsername());
            return partialCurrentUser;
        } else if (authentication instanceof JwtAuthenticationToken) {
            PartialCurrentUser partialCurrentUser = new PartialCurrentUser();
            // this is the preferred_username coming from token claims
            partialCurrentUser.setUsername(authentication.getName());
            return partialCurrentUser;
        }
        return null;
    }

}
