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

/**
 * Keeps the IAM (Keycloak) "projects" attribute of externally provisioned users in sync with the project
 * memberships managed in core. Externally provisioned users are the ones whose username ends with
 * {@value #EXTERNAL_USERNAME_SUFFIX} (LS-AAI identity).
 * <p>
 * The IAM "projects" attribute is the source of truth for the membership of these users: it is reconciled in
 * core by AuthenticationSuccessListener on every authenticated request. This service must therefore be applied
 * BEFORE (or on removal: also before) any membership change in core, so that the reconciliation does not wipe
 * the change that was just made through the core API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IamProjectMembershipService {

    private final Keycloak keycloak;

    @Value("${keycloak-client.target.realm}")
    private String realm;

    static final String EXTERNAL_USERNAME_SUFFIX = "@lifescience-ri.eu";

    /**
     * @return true if the user with the given username is externally provisioned through LS-AAI
     */
    public boolean isExternal(String username) {
        return username != null && username.endsWith(EXTERNAL_USERNAME_SUFFIX);
    }

    /**
     * Fetch the project names a user currently has access to in IAM.
     *
     * @return the project names listed in the IAM "projects" attribute of the user. Empty if the user or the
     * attribute does not exist.
     */
    public List<String> findProjects(String username) {
        Optional<UserRepresentation> user = findUser(username);
        if (user.isEmpty()) {
            return List.of();
        }
        return currentProjects(user.get());
    }

    /**
     * Add a project to the IAM "projects" attribute of a user, so that its membership survives the
     * reconciliation performed by AuthenticationSuccessListener.
     *
     * @throws UserManagementException if the user does not exist in IAM: the addition could not be materialized
     * in IAM and would be repealed at the next reconciliation
     */
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

    /**
     * Remove a project from the IAM "projects" attribute of a user. Does nothing if the user does not have this
     * project in IAM or does not exist anymore in IAM (its account was deleted).
     */
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

    /**
     * Replace the old project name by the new one in the IAM "projects" attribute of a user, after a project
     * has been renamed. Does nothing if the user does not have the old project in IAM or does not exist anymore
     * in IAM.
     */
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
