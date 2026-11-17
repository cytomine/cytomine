package be.cytomine.service.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.cytomine.exceptions.UserManagementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class IamProjectMembershipService {

    private final Keycloak keycloak;

    @Value("${keycloak-client.target.realm}")
    private String realm;

    @Value("#{'${cytomine.iam.external-username-suffixes:}'.split(',')}")
    private List<String> externalUsernameSuffixes;

    public boolean isExternal(String username) {
        return username != null && externalUsernameSuffixes.stream()
            .map(String::trim)
            .filter(suffix -> !suffix.isBlank())
            .anyMatch(username::endsWith);
    }

    public List<String> findProjects(String username) {
        return findUser(username).map(u -> currentProjects(u)).orElseGet(List::of);
    }

    public void addProject(String username, String projectName) {
        UserRepresentation user = findUser(username)
            .orElseThrow(() -> new UserManagementException(
                "Cannot add the project to the projects of the user " + username + " : user not found in IAM", 404));

        List<String> projects = new ArrayList<>(currentProjects(user));
        if (!projects.contains(projectName)) {
            projects.add(projectName);
            persistProjects(username, user, projects);
            log.info("Added project {} to the projects of external user {} in IAM", projectName, username);
        }
    }

    public void removeProject(String username, String projectName) {
        Optional<UserRepresentation> user = findUser(username);
        if (user.isEmpty()) {
            log.warn("Cannot remove project {} from the projects of external user {} in IAM: user not found",
                projectName, username);
            return;
        }

        List<String> projects = new ArrayList<>(currentProjects(user.get()));
        if (projects.contains(projectName)) {
            projects.remove(projectName);
            persistProjects(username, user.get(), projects);
            log.info("Removed project {} from the projects of external user {} in IAM", projectName, username);
        }
    }

    public void renameProject(String username, String oldName, String newName) {
        Optional<UserRepresentation> user = findUser(username);
        if (user.isEmpty()) {
            log.warn("Cannot rename project {} to {} in the projects of external user {} in IAM: user not found",
                oldName, newName, username);
            return;
        }

        List<String> projects = new ArrayList<>(currentProjects(user.get()));
        if (!projects.remove(oldName)) {
            return;
        }
        if (!projects.contains(newName)) {
            projects.add(newName);
        }
        persistProjects(username, user.get(), projects);
        log.info("Renamed project {} to {} in the projects of external user {} in IAM", oldName, newName, username);
    }

    private List<String> currentProjects(UserRepresentation user) {
        Map<String, List<String>> attributes = user.getAttributes();
        List<String> projects = attributes == null ? null : attributes.get("projects");
        return projects == null ? List.of() : projects;
    }

    private Optional<UserRepresentation> findUser(String username) {
        UsersResource usersResource = keycloak.realm(realm).users();
        return usersResource.searchByUsername(username, true).stream()
            .findFirst();
    }

    private void persistProjects(String username, UserRepresentation user, List<String> projects) {
        Map<String, List<String>> attributes =
            user.getAttributes() == null ? new HashMap<>() : user.getAttributes();
        attributes.put("projects", projects);
        user.setAttributes(attributes);

        UsersResource usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(user.getId());
        userResource.update(user);
    }
}
