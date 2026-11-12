package be.cytomine.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.domain.security.User;
import be.cytomine.mapper.UserMapper;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.security.UserService;

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_GUEST;
import static be.cytomine.common.repository.model.Role.ROLE_USER;

/**
 * Synchronizes the Cytomine user record with the JWT identity on every authentication success. All
 * database access is delegated to the domain services ({@link UserService}, {@link ProjectMemberService}):
 * this listener only parses the token attributes and orchestrates the synchronization steps.
 */
@RequiredArgsConstructor
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final CurrentRoleService currentRoleService;

    private final UserService userService;

    private final UserHttpContract userHttpContract;

    private final ProjectMemberService projectMemberService;

    private final UserMapper userMapper;

    private static Set<String> extractRolesFromAuthentication(JwtAuthenticationToken jwtAuthenticationToken) {
        Set<String> rolesFromAuthentication = new HashSet<>();
        jwtAuthenticationToken.getAuthorities().forEach((authority) -> {
            if (authority.getAuthority().equals(ROLE_USER.toString()) || authority.getAuthority()
                .equals(ROLE_ADMIN.toString()) || authority.getAuthority().equals(ROLE_GUEST.toString())) {
                rolesFromAuthentication.add(authority.getAuthority());
            }
        });
        return rolesFromAuthentication;
    }

    private static String highestRole(Set<String> rolesFromAuthentication) {
        if (rolesFromAuthentication.contains(ROLE_ADMIN.toString())) {
            return ROLE_ADMIN.toString();
        }
        if (rolesFromAuthentication.contains(ROLE_USER.toString())) {
            return ROLE_USER.toString();
        }
        return ROLE_GUEST.toString();
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            saveUserOfToken(jwtAuthenticationToken);
        }
    }

    protected void saveUserOfToken(JwtAuthenticationToken jwtAuthenticationToken) {
        Set<String> rolesFromAuthentication = extractRolesFromAuthentication(jwtAuthenticationToken);
        Map<String, String> stringTokenAttributes = jwtAuthenticationToken.getTokenAttributes()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof String)
            .map(entry ->
                Map.entry(entry.getKey(),
                    (String) entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<String>> listTokenAttributes = jwtAuthenticationToken.getTokenAttributes()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof List)
            .map(entry ->
                Map.entry(entry.getKey(),
                    ((List<?>) entry.getValue()).stream().filter(a -> a instanceof String).map(a -> (String) a)
                        .toList()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> projects = listTokenAttributes.getOrDefault("projects", List.of());
        List<String> joinTokens = listTokenAttributes.getOrDefault("lti_join_tokens", List.of());
        UUID sub = UUID.fromString(stringTokenAttributes.get("sub"));
        Optional<User> userByReference = userService.findUserByReference(sub.toString());
        Optional<User> userByUsername = userService.findUserByUsername(jwtAuthenticationToken.getName());

        if (userByUsername.isPresent() && userByReference.isEmpty()) {
            User user = userByUsername.get();
            user.setReference(sub.toString());
            user.setFirstname(
                stringTokenAttributes.get("given_name") != null ? stringTokenAttributes.get("given_name") : "");
            user.setLastname(
                stringTokenAttributes.get("family_name") != null ? stringTokenAttributes.get("family_name") : "");
            user.setEmail(stringTokenAttributes.get("email") != null ? stringTokenAttributes.get("email") : "");
            userService.save(user);

            userService.syncCumulativeRole(user, rolesFromAuthentication);
            synchronizeAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            projectMemberService.syncProjectMembership(user, projects);
            projectMemberService.syncJoinTokenMembership(user, joinTokens);

        } else if (userByReference.isEmpty()) {

            CreateUser createUser = new CreateUser(jwtAuthenticationToken.getName(),
                Optional.ofNullable(stringTokenAttributes.get("name")).map(Object::toString), Optional.of(
                stringTokenAttributes.get("given_name") != null ? stringTokenAttributes.get("given_name") : ""),
                Optional.of(
                    stringTokenAttributes.get("family_name") != null ? stringTokenAttributes.get("family_name") : ""),
                stringTokenAttributes.get("email") != null ? stringTokenAttributes.get("email") : "", Optional.empty(),
                false, highestRole(rolesFromAuthentication), "EN", Optional.empty(), Optional.empty(), null,
                Optional.of(sub.toString()));
            userHttpContract.selfRegister(createUser);

            User savedUser = userService.findUserByReference(sub.toString())
                .orElseThrow(() -> new IllegalStateException("User not found after self-registration: " + sub));
            UserResponse userResponse = userMapper.map(savedUser);

            projectMemberService.syncProjectMembership(savedUser, projects);
            projectMemberService.syncJoinTokenMembership(savedUser, joinTokens);

            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }

        } else {
            User user = userByReference.get();
            userService.syncCumulativeRole(user, rolesFromAuthentication);
            synchronizeAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            projectMemberService.syncProjectMembership(user, projects);
            projectMemberService.syncJoinTokenMembership(user, joinTokens);
        }
    }

    private void synchronizeAdminSession(JwtAuthenticationToken jwtAuthenticationToken, User user,
        Set<String> rolesFromAuthentication) {
        if (rolesFromAuthentication.contains(ROLE_ADMIN.toString())) {
            UserResponse userResponse = userMapper.map(user);
            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
