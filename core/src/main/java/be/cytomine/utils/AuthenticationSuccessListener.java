package be.cytomine.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.security.IamProjectMembershipService;

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_GUEST;
import static be.cytomine.common.repository.model.Role.ROLE_USER;

@RequiredArgsConstructor
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final ApplicationContext applicationContext;

    private final CurrentRoleService currentRoleService;

    private final SecUserSecRoleRepository secSecUserSecRoleRepository;

    private final SecRoleRepository secRoleRepository;

    private final UserHttpContract userHttpContract;

    private final UserRepository userRepository;

    private final ProjectRepository projectRepository;

    private final ProjectMemberService projectMemberService;

    private final UserMapper userMapper;

    private final IamProjectMembershipService iamProjectMembershipService;

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

    private AuthenticationSuccessListener self() {
        return applicationContext.getBean(AuthenticationSuccessListener.class);
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

        UUID sub = UUID.fromString(stringTokenAttributes.get("sub"));
        Optional<User> userByReference = userRepository.findByReference(sub.toString());
        Optional<User> userByUsername = userRepository.findByUsername(jwtAuthenticationToken.getName());
        if (userByUsername.isPresent() && userByReference.isEmpty()) {
            User user = userByUsername.get();
            user.setReference(sub.toString());
            user.setFirstname(
                stringTokenAttributes.get("given_name") != null ? stringTokenAttributes.get("given_name") : "");
            user.setLastname(
                stringTokenAttributes.get("family_name") != null ? stringTokenAttributes.get("family_name") : "");
            user.setEmail(stringTokenAttributes.get("email") != null ? stringTokenAttributes.get("email") : "");
            userRepository.save(user);

            self().updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self().updateProjectsMembership(user);

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

            User savedUser = userRepository.findByReference(sub.toString())
                .orElseThrow(() -> new IllegalStateException("User not found after self-registration: " + sub));
            UserResponse userResponse = userMapper.map(savedUser);

            self().updateProjectsMembership(savedUser);

            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }

        } else {
            User user = userByReference.get();
            self().updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self().updateProjectsMembership(user);
        }
    }

    @Transactional
    protected void updateRolesAndAdminSession(JwtAuthenticationToken jwtAuthenticationToken, User user,
        Set<String> rolesFromAuthentication) {

        secSecUserSecRoleRepository.deleteAllByIdInBatch(
            secSecUserSecRoleRepository.findAllBySecUser(user).stream().map(SecUserSecRole::getId).toList());
        secSecUserSecRoleRepository.flush();
        // Guest > User > Admin
        self().setCumulativeRole(rolesFromAuthentication, user);
        UserResponse userResponse = userMapper.map(user);
        if (rolesFromAuthentication.contains(ROLE_ADMIN.toString())) {
            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }
        }
    }

    /**
     * Reconcile the project membership of the user in core with the one provisioned in IAM (the "projects"
     * attribute of the user in Keycloak). <br>
     * The IAM state is read live instead of using the "projects" claim of the request token: the token keeps
     * the claim value it was issued with, which can be outdated by the time the request is processed (e.g.
     * right after a member was added/removed through core).
     */
    @Transactional
    protected void updateProjectsMembership(User user) {
        if (!iamProjectMembershipService.isExternal(user.getUsername())) {
            return; // do nothing if user is local
        }

        List<Project> permittedUserProjects = projectRepository.findByNameIn(
            iamProjectMembershipService.findProjects(user.getUsername()));
        List<Project> actualUserProjects = projectRepository.findAllProjectForUser(user.getUsername());

        List<Project> projectsToAdd =
            permittedUserProjects.stream().filter(p -> !actualUserProjects.contains(p)).toList();
        for (Project project : projectsToAdd) {
            projectMemberService.addUserToProjectWithAdmin(user.getUsername(), project, false);
        }

        List<Project> projectsToRemove =
            actualUserProjects.stream().filter(p -> !permittedUserProjects.contains(p)).toList();

        for (Project project : projectsToRemove) {
            projectMemberService.deleteUserFromProjectWithAdmin(user, project, false);
        }
    }

    @Transactional
    protected void setCumulativeRole(Set<String> rolesFromAuthentication, User user) {

        SecUserSecRole secSecUserSecRole = new SecUserSecRole();
        if (rolesFromAuthentication.contains(ROLE_ADMIN.toString())) {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority(ROLE_ADMIN.toString()));
        } else if (rolesFromAuthentication.contains(ROLE_USER.toString())) {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority(ROLE_USER.toString()));
        } else {
            secSecUserSecRole.setSecRole(secRoleRepository.getByAuthority(ROLE_GUEST.toString()));
        }
        secSecUserSecRole.setSecUser(user);
        secSecUserSecRoleRepository.save(secSecUserSecRole);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
