package be.cytomine.service.meta;

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
import be.cytomine.domain.command.EditCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.repository.meta.TagDomainAssociationRepository;
import be.cytomine.repository.meta.TagRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class TagService extends ModelService {

    private final TagRepository tagRepository;

    private final TagDomainAssociationRepository tagDomainAssociationRepository;

    private final SecurityACLService securityACLService;

    private final CurrentUserService currentUserService;

    private final TagDomainAssociationService tagDomainAssociationService;

    @Override
    public Class currentDomain() {
        return Tag.class;
    }

    public List<Tag> list() {
        securityACLService.checkGuest();
        return tagRepository.findAll();
    }

    public Tag get(Long id) {
        return find(id).orElse(null);
    }

    public Optional<Tag> find(Long id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }

    @Override
    public CommandResponse add(JsonObject jsonObject) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        jsonObject.put("user", currentUser.getId());
        return executeCommand(new AddCommand(currentUser), null, jsonObject);
    }

    @Override
    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkIsCreator(domain, currentUser);
        if (tagDomainAssociationRepository.countByTag((Tag) domain) > 0) {
            //if not admin then check if there is no association
            securityACLService.checkAdmin(currentUser);
        }
        return executeCommand(new EditCommand(currentUser, transaction), domain, jsonNewData);
    }

    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        UserResponse currentUser = currentUserService.getCurrentUser();
        securityACLService.checkIsCreator(domain, currentUser);
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c, domain, null);
    }


    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new Tag().buildDomainFromJson(json, getEntityManager());
    }

    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((Tag) domain).getName());
    }

    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        Tag tag = (Tag) domain;
        if (tag != null && tag.getName() != null) {
            if (tagRepository.findByNameIgnoreCase(tag.getName())
                .stream()
                .anyMatch(x -> !Objects.equals(x.getId(), tag.getId()))) {
                throw new AlreadyExistException("Tag " + tag.getName() + " already exist!");
            }
        }
    }

    public void deleteDependencies(CytomineDomain domain, Transaction transaction, Task task) {
        deleteDependentTagDomainAssociation((Tag) domain, transaction, task);
    }

    private void deleteDependentTagDomainAssociation(Tag tag, Transaction transaction, Task task) {
        for (TagDomainAssociation tagDomainAssociation : tagDomainAssociationRepository.findAllByTag(tag)) {
            tagDomainAssociationService.delete(tagDomainAssociation, transaction, task, false);
        }
    }
}
