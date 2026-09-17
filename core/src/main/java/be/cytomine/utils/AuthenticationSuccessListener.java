package be.cytomine.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    private static Set<String> extractRolesFromAuthentication(JwtAuthenticationToken jwtAuthenticationToken) {
        Set<String> rolesFromAuthentication = new HashSet<>();
        jwtAuthenticationToken.getAuthorities().forEach((authority) -> {
            if (authority.getAuthority().equals("ROLE_USER")
                || authority.getAuthority().equals("ROLE_ADMIN")
                || authority.getAuthority().equals("ROLE_GUEST")) {
                rolesFromAuthentication.add(authority.getAuthority());
            }
        });
        return rolesFromAuthentication;
    }

    private AuthenticationSuccessListener self() {
        return applicationContext.getBean(AuthenticationSuccessListener.class);
    }

    private static String highestRole(Set<String> rolesFromAuthentication) {
        if (rolesFromAuthentication.contains("ROLE_ADMIN")) {
            return "ROLE_ADMIN";
        }
        if (rolesFromAuthentication.contains("ROLE_USER")) {
            return "ROLE_USER";
        }
        return "ROLE_GUEST";
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            saveUserOfToken(jwtAuthenticationToken);
        }
    }

    protected void saveUserOfToken(JwtAuthenticationToken jwtAuthenticationToken) {
        Set<String> rolesFromAuthentication = extractRolesFromAuthentication(jwtAuthenticationToken);
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        List<String> projects = (List<String>) tokenAttributes.getOrDefault("projects", Collections.emptyList());
        UUID sub = UUID.fromString(tokenAttributes.get("sub").toString());
        Optional<User> userByReference = userRepository.findByReference(sub.toString());
        Optional<User> userByUsername = userRepository.findByUsername(jwtAuthenticationToken.getName());
        if (userByUsername.isPresent() && userByReference.isEmpty()) {
            User user = userByUsername.get();
            user.setReference(sub.toString());
            user.setFirstname(tokenAttributes.get("given_name") != null
                ? tokenAttributes.get("given_name").toString() : "");
            user.setLastname(tokenAttributes.get("family_name") != null
                ? tokenAttributes.get("family_name").toString() : "");
            user.setEmail(tokenAttributes.get("email") != null ? tokenAttributes.get("email").toString() : "");
            userRepository.save(user);

            self().updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self().updateProjectsMembership(projects, user);

        } else if (userByReference.isEmpty()) {

            CreateUser createUser = new CreateUser(
                jwtAuthenticationToken.getName(),
                Optional.ofNullable(tokenAttributes.get("name")).map(Object::toString),
                Optional.of(tokenAttributes.get("given_name") != null
                    ? tokenAttributes.get("given_name").toString() : ""),
                Optional.of(tokenAttributes.get("family_name") != null
                    ? tokenAttributes.get("family_name").toString() : ""),
                tokenAttributes.get("email") != null ? tokenAttributes.get("email").toString() : "",
                Optional.empty(),
                false,
                highestRole(rolesFromAuthentication),
                "EN",
                Optional.empty(),
                Optional.empty(),
                null,
                Optional.of(sub.toString())
            );
            userHttpContract.selfRegister(createUser);

            User savedUser = userRepository.findByReference(sub.toString())
                .orElseThrow(() -> new IllegalStateException("User not found after self-registration: " + sub));
            UserResponse userResponse = userMapper.map(savedUser);

            self().updateProjectsMembership(projects, savedUser);

            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }

        } else {
            User user = userByReference.get();
            self().updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self().updateProjectsMembership(projects, user);
        }
    }

    @Transactional
    protected void updateRolesAndAdminSession(
        JwtAuthenticationToken jwtAuthenticationToken,
        User user,
        Set<String> rolesFromAuthentication
    ) {

        secSecUserSecRoleRepository.deleteAllByIdInBatch(
            secSecUserSecRoleRepository.findAllBySecUser(user).stream()
                .map(SecUserSecRole::getId).toList());
        secSecUserSecRoleRepository.flush();
        // Guest > User > Admin
        self().setCumulativeRole(rolesFromAuthentication, user);
        UserResponse userResponse = userMapper.map(user);
        if (rolesFromAuthentication.contains("ROLE_ADMIN")) {
            if (currentRoleService.hasCurrentUserAdminRole(userResponse)) {
                currentRoleService.activeAdminSession(userResponse, jwtAuthenticationToken);
            }
        }
    }

    @Transactional
    protected void updateProjectsMembership(List<String> projects, User user) {
        if (!isExternal(user)) {
            return; // do nothing if user is local
        }

        List<Project> permittedUserProjects = projectRepository.findByNameIn(projects);
        List<Project> actualUserProjects = projectRepository.findAllProjectForUser(user.getUsername());

        List<Project> projectsToAdd = permittedUserProjects.stream()
            .filter(p -> !actualUserProjects.contains(p))
            .toList();
        for (Project project : projectsToAdd) {
            projectMemberService.addUserToProjectWithAdmin(user, project, false);
        }

        List<Project> projectsToRemove = actualUserProjects.stream()
            .filter(p -> !permittedUserProjects.contains(p))
            .toList();

        for (Project project : projectsToRemove) {
            projectMemberService.deleteUserFromProjectWithAdmin(user, project, false);
        }
    }

    private boolean isExternal(User user) {
        return user.getUsername().endsWith("@lifescience-ri.eu");
    }

    @Transactional
    protected void setCumulativeRole(Set<String> rolesFromAuthentication, User user) {

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

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
