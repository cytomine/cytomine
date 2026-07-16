package be.cytomine.service.image.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.AddCommand;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.EditCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.repository.image.group.ImageGroupImageInstanceRepository;
import be.cytomine.repository.image.group.ImageGroupRepository;
import be.cytomine.repository.ontology.AnnotationGroupRepository;
import be.cytomine.repository.ontology.AnnotationLinkRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.UrlApi;
import be.cytomine.service.command.TransactionService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ImageGroupService extends ModelService {

    private final AnnotationGroupRepository annotationGroupRepository;

    private final AnnotationLinkRepository annotationLinkRepository;

    private final CurrentUserService currentUserService;

    private final ImageGroupRepository imageGroupRepository;

    private final ImageGroupImageInstanceRepository imageGroupImageInstanceRepository;

    private final SecurityACLService securityACLService;

    private final TransactionService transactionService;

    @Override
    public Class currentDomain() {
        return ImageGroup.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new ImageGroup().buildDomainFromJson(json, getEntityManager());
    }

    @Override
    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((ImageGroup) domain).getName(), ((ImageGroup) domain).getProject().getName());
    }

    public Optional<ImageGroup> find(Long id) {
        Optional<ImageGroup> imageGroup = imageGroupRepository.findById(id);
        imageGroup.ifPresent(group -> securityACLService.check(group.container(), READ));
        return imageGroup;
    }

    public ImageGroup get(Long id) {
        return find(id).orElse(null);
    }

    public List<ImageGroup> list(Project project) {
        securityACLService.check(project, READ);

        List<ImageGroup> groups = imageGroupRepository.findAllByProject(project);
        for (ImageGroup group : groups) {
            List<Object> images = new ArrayList<>();
            for (ImageGroupImageInstance igii : imageGroupImageInstanceRepository.findAllByGroup(group)) {
                images.add(Map.of(
                    "id", igii.getImage().getId(),
                    "instanceFilename", igii.getImage().getBlindInstanceFilename(),
                    "thumb", UrlApi.getImageInstanceThumbUrlWithMaxSize(igii.getImage().getId()),
                    "width", igii.getImage().getBaseImage().getWidth(),
                    "height", igii.getImage().getBaseImage().getHeight()
                ));
            }
            group.setImages(images);
        }

        return groups;
    }

    public CommandResponse add(JsonObject json) {
        transactionService.start();
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(json.getJSONAttrLong("project"), Project.class, READ);

        return executeCommand(new AddCommand(currentUser), null, json);
    }

    @Override
    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(domain.container(), READ);

        return executeCommand(new EditCommand(currentUser, transaction), domain, jsonNewData);
    }

    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(domain.container(), READ);

        return executeCommand(new DeleteCommand(currentUser, transaction), domain, null);
    }

    protected void beforeDelete(CytomineDomain domain) {
        ImageGroup imageGroup = (ImageGroup) domain;

        List<AnnotationGroup> annotationGroups = annotationGroupRepository.findAllByImageGroup(imageGroup);
        for (AnnotationGroup annotationGroup : annotationGroups) {
            annotationLinkRepository.deleteAllByGroup(annotationGroup);
        }

        annotationGroupRepository.deleteAllByImageGroup(imageGroup);
        imageGroupImageInstanceRepository.deleteAllByGroup(imageGroup);
    }
}
