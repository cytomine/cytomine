package be.cytomine.utils;

import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.SecRole;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.image.server.StorageService;
import be.cytomine.service.project.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
@Slf4j
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private AuthenticationSuccessListener self; // necessary, otherwise spring will bypass the proxy for transactional

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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuthenticationToken)
            saveUserOfToken(jwtAuthenticationToken);

    }

    protected void saveUserOfToken(JwtAuthenticationToken jwtAuthenticationToken) {

        Set<String> rolesFromAuthentication =
            extractRolesFromAuthentication(jwtAuthenticationToken);
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        List<String> projects = (List<String>) tokenAttributes.getOrDefault("projects", Collections.emptyList());
        UUID sub = UUID.fromString(tokenAttributes.get("sub").toString());
        Optional<User> userByReference = userRepository.findByReference(sub.toString());
        Optional<User> userByUsername = userRepository.findByUsername(jwtAuthenticationToken.getName());
        if (userByUsername.isPresent() && userByReference.isEmpty()) {
            User user = userByUsername.get();
            user.setReference(sub.toString());
            userRepository.save(user);

            self.updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self.updateProjectsMembership(projects, user);

        } else if (userByReference.isEmpty()) {

            User newUser = new User();
            newUser.setUsername(jwtAuthenticationToken.getName());
            newUser.setReference(sub.toString());
            newUser.setName(tokenAttributes.get("name").toString());
            // generate keys for public/private keys authentication
            newUser.generateKeys();

            //save domain into the database
            User savedUser = userRepository.save(newUser);
            self.setCumulativeRole(rolesFromAuthentication, savedUser);
            storageService.initUserStorage(savedUser);

            self.updateProjectsMembership(projects, savedUser);

            savedUser = userRepository.findByReference(sub.toString()).orElse(null);
            if (currentRoleService.hasCurrentUserAdminRole(savedUser)) {
                currentRoleService.activeAdminSession(savedUser, jwtAuthenticationToken);
            }

        } else {
            User user = userByReference.get();
            self.updateRolesAndAdminSession(jwtAuthenticationToken, user, rolesFromAuthentication);
            self.updateProjectsMembership(projects, user);
        }
    }

    @Transactional
    protected void updateRolesAndAdminSession(JwtAuthenticationToken jwtAuthenticationToken,
                                              User user,
                                              Set<String> rolesFromAuthentication) {

        secSecUserSecRoleRepository.deleteAllByIdInBatch(
            secSecUserSecRoleRepository.findAllBySecUser(user).stream()
                .map(SecUserSecRole::getId).toList());
        secSecUserSecRoleRepository.flush();
        // Guest > User > Admin
        self.setCumulativeRole(rolesFromAuthentication, user);

        if (rolesFromAuthentication.contains("ROLE_ADMIN")) {
            if (currentRoleService.hasCurrentUserAdminRole(user)) {
                currentRoleService.activeAdminSession(user, jwtAuthenticationToken);
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
