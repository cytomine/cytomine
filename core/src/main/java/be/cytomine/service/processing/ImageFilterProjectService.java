package be.cytomine.service.processing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.AddCommand;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.processing.ImageFilter;
import be.cytomine.domain.processing.ImageFilterProject;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.processing.ImageFilterProjectRepository;
import be.cytomine.repository.processing.ImageFilterRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ImageFilterProjectService extends ModelService {

    private final CurrentUserService currentUserService;

    private final ImageFilterProjectRepository imageFilterProjectRepository;

    private final ImageFilterRepository imageFilterRepository;

    private final ProjectRepository projectRepository;

    private final SecurityACLService securityACLService;

    @Override
    public Class currentDomain() {
        return ImageFilterProjectService.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new ImageFilterProject().buildDomainFromJson(json, getEntityManager());
    }

    public List<ImageFilterProject> list() {
        securityACLService.checkAdmin(currentUserService.getCurrentUser());
        return imageFilterProjectRepository.findAll();
    }

    public List<ImageFilterProject> list(Project project) {
        securityACLService.check(project, READ);
        return imageFilterProjectRepository.findAllByProject(project);
    }

    public Optional<ImageFilterProject> find(ImageFilter imageFilter, Project project) {
        securityACLService.check(project, READ);
        return imageFilterProjectRepository.findByImageFilterAndProject(imageFilter, project);
    }

    @Override
    public CommandResponse add(JsonObject jsonObject) {

        Project project = projectRepository.findById(jsonObject.getJSONAttrLong("project"))
            .orElseThrow(() -> new ObjectNotFoundException("Project", jsonObject.getJSONAttrStr("project")));
        ImageFilter imageFilter = imageFilterRepository.findById(jsonObject.getJSONAttrLong("imageFilter"))
            .orElseThrow(() -> new ObjectNotFoundException("ImageFilter", jsonObject.getJSONAttrStr("imageFilter")));

        securityACLService.check(project, ADMINISTRATION);

        return executeCommand(new AddCommand(currentUserService.getCurrentUser()), null, jsonObject);
    }

    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain.container(), ADMINISTRATION);
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c, domain, null);
    }

    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        ImageFilterProject imageFilterProject = (ImageFilterProject) domain;
        return List.of(imageFilterProject.getImageFilter().getName(), imageFilterProject.getProject().getName());
    }

    /**
     * Retrieve domain thanks to a JSON object
     *
     * @return domain retrieve thanks to json
     */
    @Override
    public CytomineDomain retrieve(JsonObject json) {
        if (json.containsKey("id")) {
            return imageFilterProjectRepository.findById(json.getId())
                .orElseThrow(() -> new ObjectNotFoundException("ImageFilterProject", json.toJsonString()));
        }
        ImageFilter imageFilter = imageFilterRepository.getById(json.getJSONAttrLong("imageFilter"));
        Project project = projectRepository.getById(json.getJSONAttrLong("project"));
        return imageFilterProjectRepository.findByImageFilterAndProject(imageFilter, project)
            .orElseThrow(() -> new ObjectNotFoundException("ImageFilterProject", json.toJsonString()));
    }

    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        ImageFilterProject imageFilterProject = (ImageFilterProject) domain;
        if (domain != null) {
            if (imageFilterProjectRepository.findByImageFilterAndProject(
                    imageFilterProject.getImageFilter(),
                    imageFilterProject.getProject()
                )
                .stream().anyMatch(x -> !Objects.equals(x.getId(), imageFilterProject.getId()))) {
                throw new AlreadyExistException("ImageFilter "
                    + imageFilterProject.getImageFilter().getName()
                    + " already linked to project "
                    + imageFilterProject.getProject().getName());
            }
        }
    }
}
