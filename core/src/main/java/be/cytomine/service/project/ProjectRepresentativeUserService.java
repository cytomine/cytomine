package be.cytomine.service.project;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.AddCommand;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.repository.project.ProjectRepresentativeUserRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.service.security.UserService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProjectRepresentativeUserService extends ModelService {

    private final ProjectRepresentativeUserRepository projectRepresentativeUserRepository;

    private final SecurityACLService securityACLService;

    private final CurrentUserService currentUserService;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    public Class currentDomain() {
        return ProjectRepresentativeUser.class;
    }

    public ProjectRepresentativeUser get(Long id) {
        return find(id).orElse(null);
    }

    public Optional<ProjectRepresentativeUser> find(Long id) {
        Optional<ProjectRepresentativeUser>
            optionalProjectRepresentativeUser
            = projectRepresentativeUserRepository.findById(id);
        optionalProjectRepresentativeUser.ifPresent(projectRepresentativeUser -> securityACLService.check(
            projectRepresentativeUser,
            READ
        ));
        return optionalProjectRepresentativeUser;
    }

    public Optional<ProjectRepresentativeUser> find(Project project, User user) {
        Optional<ProjectRepresentativeUser>
            optionalProjectRepresentativeUser
            = projectRepresentativeUserRepository.findByProjectAndUser(project, user);
        optionalProjectRepresentativeUser.ifPresent(projectRepresentativeUser -> securityACLService.check(
            projectRepresentativeUser,
            READ
        ));
        return optionalProjectRepresentativeUser;
    }

    public List<ProjectRepresentativeUser> listByProject(Project project) {
        securityACLService.check(project, READ);
        return projectRepresentativeUserRepository.findAllByProject(project);
    }

    public List<ProjectRepresentativeUser> listByProjectWithAdmin(Project project) {
        return projectRepresentativeUserRepository.findAllByProject(project);
    }


    @Override
    public CommandResponse add(JsonObject jsonObject) {
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.check(jsonObject.getJSONAttrLong("project"), Project.class, WRITE);
        Long userId = jsonObject.getJSONAttrLong("user");
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("User", userId));
        Project project = projectRepository.findById(jsonObject.getJSONAttrLong("project"))
            .orElseThrow(() -> new ObjectNotFoundException("Project", jsonObject.getJSONAttrStr("project")));

        securityACLService.checkIsUserInProject(user, project);

        return executeCommand(new AddCommand(currentUser), null, jsonObject);
    }

    public CommandResponse add(JsonObject jsonObject, User adminAsCurrent) {
        securityACLService.check(jsonObject.getJSONAttrLong("project"), Project.class, WRITE);
        User user = userService.findUser(jsonObject.getJSONAttrLong("user"))
            .orElseThrow(() -> new ObjectNotFoundException("User", jsonObject.getJSONAttrStr("user")));
        Project project = projectRepository.findById(jsonObject.getJSONAttrLong("project"))
            .orElseThrow(() -> new ObjectNotFoundException("Project", jsonObject.getJSONAttrStr("project")));

        securityACLService.checkIsUserInProject(user, project);

        return executeCommand(new AddCommand(adminAsCurrent), null, jsonObject);
    }


    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        if (listByProject(((ProjectRepresentativeUser) domain).getProject()).size() < 2) {
            throw new WrongArgumentException(
                "You cannot remove the last representative role. Add someone else as representative");
        }
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain.container(), WRITE);
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c, domain, null);
    }

    public CommandResponse deleteWithAdmin(
        CytomineDomain domain,
        Transaction transaction,
        Task task,
        boolean printMessage,
        User currentAsAdmin) {
        if (listByProject(((ProjectRepresentativeUser) domain).getProject()).size() < 2) {
            throw new WrongArgumentException("You cannot remove the last representative role. "
                + "Add someone else as representative");
        }
        securityACLService.check(domain.container(), WRITE);
        Command c = new DeleteCommand(currentAsAdmin, transaction);
        return executeCommand(c, domain, null);
    }


    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new ProjectRepresentativeUser().buildDomainFromJson(json, getEntityManager());
    }

    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        User user = ((ProjectRepresentativeUser) domain).getUser();
        return List.of(domain.getId(), user.getFullName());
    }


    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        ProjectRepresentativeUser projectRepresentativeUser = (ProjectRepresentativeUser) domain;
        if (projectRepresentativeUser != null) {
            if (projectRepresentativeUserRepository.findByProjectAndUser(
                projectRepresentativeUser.getProject(),
                projectRepresentativeUser.getUser()
            ).stream().anyMatch(x -> !Objects.equals(x.getId(), projectRepresentativeUser.getId()))) {
                throw new AlreadyExistException("User "
                    + projectRepresentativeUser.getUser().getId()
                    + " is already representative of the project "
                    + projectRepresentativeUser.getProject().getId());
            }
        }
    }

}
