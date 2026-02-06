package be.cytomine.utils;

import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.image.server.StorageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    SecUserSecRoleRepository secSecUserSecRoleRepository;

    @Autowired
    private SecRoleRepository secRoleRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private CurrentRoleService currentRoleService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken)
            saveUserOfToken(jwtAuthenticationToken);

    }

    @Transactional
    protected void saveUserOfToken(JwtAuthenticationToken jwtAuthenticationToken) {

        Set<String> rolesFromAuthentication =
            extractRolesFromAuthentication(jwtAuthenticationToken);
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        UUID sub = UUID.fromString(tokenAttributes.get("sub").toString());
        Optional<User> userByReference = userRepository.findByReference(sub.toString());
        if (userByReference.isEmpty()) {

            User newUser = new User();
            newUser.setUsername(jwtAuthenticationToken.getName());
            newUser.setReference(sub.toString());
            newUser.setName(tokenAttributes.get("name").toString());
            // generate keys for public/private keys authentication
            newUser.generateKeys();

            //save domain into the database
            User savedUser = userRepository.save(newUser);
            // Guest > User > Admin
            setCumulativeRole(rolesFromAuthentication, savedUser);
            // create storage for the user
            storageService.initUserStorage(savedUser);

            // activate admin session for user
            savedUser = userRepository.findByReference(sub.toString()).orElse(null);
            if (currentRoleService.hasCurrentUserAdminRole(savedUser))
            {
                currentRoleService.activeAdminSession(savedUser, jwtAuthenticationToken);
            }


        } else {
            // update authorities
            User user = userByReference.get();
            // remove all roles of user
            secSecUserSecRoleRepository.deleteAllByIdInBatch(
                secSecUserSecRoleRepository.findAllBySecUser(user).stream()
                    .map(SecUserSecRole::getId).toList());
            secSecUserSecRoleRepository.flush();
            // Guest > User > Admin
            setCumulativeRole(rolesFromAuthentication, user);

            if (rolesFromAuthentication.contains("ADMIN")) {
                if (currentRoleService.hasCurrentUserAdminRole(user)) {
                    currentRoleService.activeAdminSession(user, jwtAuthenticationToken);
                }
            }
        }
    }

    private void setCumulativeRole(Set<String> rolesFromAuthentication, User user) {

        SecUserSecRole secSecUserSecRole = new SecUserSecRole();
        if (rolesFromAuthentication.contains("ROLE_ADMIN")) {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority("ROLE_ADMIN"));
        } else if (rolesFromAuthentication.contains("ROLE_USER")) {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority("ROLE_USER"));
        } else {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority("ROLE_GUEST"));
        }
        secSecUserSecRole.setSecUser(user);
        secSecUserSecRoleRepository.save(secSecUserSecRole);
    }

    private static Set<String> extractRolesFromAuthentication(JwtAuthenticationToken jwtAuthenticationToken) {
        Set<String> rolesFromAuthentication = new HashSet<>();
        jwtAuthenticationToken.getAuthorities().forEach((authority) -> {
            if (authority.getAuthority().equals("ROLE_USER") || authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_GUEST")) {
                rolesFromAuthentication.add(authority.getAuthority());
            }
        });
        return rolesFromAuthentication;
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
