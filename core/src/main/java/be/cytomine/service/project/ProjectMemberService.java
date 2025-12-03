package be.cytomine.service.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.User;
import be.cytomine.dto.NamedCytomineDomain;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.security.SecurityACLService;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ProjectMemberService {

    private final CurrentUserService currentUserService;

    private final PermissionService permissionService;

    private final ProjectRepository projectRepository;

    private final ProjectRepresentativeUserService projectRepresentativeUserService;

    private final SecurityACLService securityACLService;

    public void addUserToProject(User user, Project project, boolean admin) {
        securityACLService.check(project, ADMINISTRATION);
        log.info("service.addUserToProject");
        if (project != null) {
            log.info("addUserToProject project=" + project + " user=" + user + " ADMIN=" + admin);
            synchronized (this.getClass()) {
                if (admin) {
                    permissionService.addPermission(project, user.getUsername(), ADMINISTRATION);
                }
                permissionService.addPermission(project, user.getUsername(), READ);
                if (project.getOntology() != null) {
                    log.info(
                            "addUserToProject ontology=" + project.getOntology() + " user=" + user + " ADMIN=" + admin);
                    permissionService.addPermission(project.getOntology(), user.getUsername(), READ);
                    if (admin) {
                        permissionService.addPermission(project.getOntology(), user.getUsername(), ADMINISTRATION);
                    }
                }
            }
        }
    }

    public void deleteUserFromProject(User user, Project project, boolean admin) {
        if (!Objects.equals(currentUserService.getCurrentUser().getId(), user.getId())) {
            securityACLService.check(project, ADMINISTRATION);
        }
        if (project != null) {
            log.info("deleteUserFromProject project=" + project.getId() + " username=" + user.getUsername() + " ADMIN=" + admin);

            log.info("deleteUserFromProject BEFORE ADMINISTRATION=" + permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION));
            log.info("deleteUserFromProject BEFORE READ=" + permissionService.hasACLPermission(project, user.getUsername(), READ));
            if (admin) {
                permissionService.deletePermission(project, user.getUsername(), ADMINISTRATION);
            } else {
                permissionService.deletePermission(project, user.getUsername(), READ);
            }
            log.info("deleteUserFromProject AFTER ADMINISTRATION=" + permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION));
            log.info("deleteUserFromProject AFTER READ=" + permissionService.hasACLPermission(project, user.getUsername(), READ));
            if (!permissionService.hasACLPermission(project, user.getUsername(), READ) && project.getOntology() != null) {
                removeOntologyRightIfNecessary(project, (User) user, admin);
            }
            // if no representative, add current user as a representative
            boolean hasLostAccessToProject = (!permissionService.hasACLPermission(project, user.getUsername(), READ) && !permissionService.hasACLPermission(project, user.getUsername(), READ));
            boolean isLastRepresentative = projectRepresentativeUserService.listByProject(project).size() == 1 && projectRepresentativeUserService.listByProject(project).get(0).getUser().getId().equals(user.getId());
            if (hasLostAccessToProject && isLastRepresentative) {
                if (!securityACLService.getProjectList(currentUserService.getCurrentUser(), null).contains(project)) {
                    // if current user is not in project (= SUPERADMIN), add to the project
                    addUserToProject(currentUserService.getCurrentUser(), project, true);
                }
                log.info("add current user " + currentUserService.getCurrentUsername() + " as representative for project " + project.getId());
                ProjectRepresentativeUser pru = new ProjectRepresentativeUser();
                pru.setProject(project);
                pru.setUser((User) currentUserService.getCurrentUser());
                projectRepresentativeUserService.add(pru.toJsonObject());

                projectRepresentativeUserService.find(project, (User) user).ifPresent(x -> projectRepresentativeUserService.delete(x, null, null, false));

            }
            log.info("deleteUserFromProject " + permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION));
        }
    }

    private void removeOntologyRightIfNecessary(Project project, User user, boolean admin) {
        // we remove the right ONLY if user has no other project with this ontology
        List<Project> projects = securityACLService.getProjectList(user, project.getOntology());
        List<Project> otherProjects = new ArrayList<>(projects);
        otherProjects.remove(project);

        if (otherProjects.isEmpty()) {
            // user has no other project with this ontology, remove the right!
            permissionService.deletePermission(project.getOntology(), user.getUsername(), READ);
            permissionService.deletePermission(project.getOntology(), user.getUsername(), ADMINISTRATION);
        } else if (admin) {
            List<Long> managedProjectList = projectRepository.listByAdmin(user).stream().map(NamedCytomineDomain::getId).toList();
            List<Long> otherProjectsIds = otherProjects.stream().map(CytomineDomain::getId).toList();
            if (managedProjectList.stream().noneMatch(otherProjectsIds::contains)) {
                permissionService.deletePermission(project.getOntology(), user.getUsername(), ADMINISTRATION);
            }
        }
    }
}
