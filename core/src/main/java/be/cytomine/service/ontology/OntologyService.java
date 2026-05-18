package be.cytomine.service.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.AddCommand;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.EditCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.ontology.OntologyExport;
import be.cytomine.dto.ontology.TermSummary;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ConstraintException;
import be.cytomine.repository.ontology.OntologyRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.service.security.UserService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.springframework.security.acls.domain.BasePermission.DELETE;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OntologyService extends ModelService {

    private final CurrentUserService currentUserService;
    private final OntologyRepository ontologyRepository;
    private final PermissionService permissionService;
    private final ProjectRepository projectRepository;
    private final SecurityACLService securityACLService;
    private final TermHttpContract termHttpContract;
    private final TermRelationHttpContract termRelationHttpContract;
    private final TermService termService;
    private final UserService userService;

    public Ontology get(Long id) {
        return find(id).orElse(null);
    }

    public Optional<Ontology> find(Long id) {
        Optional<Ontology> optionalOntology = ontologyRepository.findById(id);
        optionalOntology.ifPresent(ontology -> securityACLService.check(ontology, READ));
        return optionalOntology;
    }

    /**
     * List ontology with full tree structure (term, relation,...) Security check is done inside method
     */
    public List<Ontology> list() {
        return securityACLService.getOntologyList(currentUserService.getCurrentUser());
    }

    /**
     * List ontology with just id/name Security check is done inside method
     */
    public List<JsonObject> listLight() {
        List<Ontology> ontologies = list();
        List<JsonObject> data = new ArrayList<>();
        for (Ontology ontology : ontologies) {
            data.add(JsonObject.of("id", ontology.getId(), "name", ontology.getName()));
        }
        return data;
    }

    @Override
    public CommandResponse add(JsonObject jsonObject) {
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        jsonObject.put("user", currentUser.getId());
        return executeCommand(new AddCommand(currentUser), null, jsonObject);
    }

    @Override
    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain, WRITE);
        securityACLService.checkUser(currentUser);
        return executeCommand(new EditCommand(currentUser, transaction), domain, jsonNewData);
    }

    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain, DELETE);
        securityACLService.checkUser(currentUser);

        Set<Long> allTermRelationIdsByOntology = termRelationHttpContract.findAllIdsByOntologyId(domain.getId(),
            currentUser.getId());
        if (!allTermRelationIdsByOntology.isEmpty()) {
            termRelationHttpContract.deleteAll(allTermRelationIdsByOntology, currentUser.getId());
        }

        Set<Long> allTermIdsByOntology = termHttpContract.findAllTermIdsByOntology(domain.getId(), currentUser.getId());
        if (!allTermIdsByOntology.isEmpty()) {
            termHttpContract.deleteAll(allTermIdsByOntology, currentUser.getId());
        }

        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c, domain, null);
    }

    @Override
    public Class currentDomain() {
        return Ontology.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new Ontology().buildDomainFromJson(json, getEntityManager());
    }

    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((Ontology) domain).getName());
    }

    public void determineRightsForUsers(Ontology ontology, List<User> users) {
        for (User user : users) {
            determineRightsForUser(ontology, user);
        }
    }

    public void determineRightsForUser(Ontology ontology, User user) {
        List<Project> projects = projectRepository.findAllByOntologyId(ontology.getId());
        if (projects.stream().anyMatch(project -> userService.listAdmins(project).contains(user))) {
            permissionService.addPermission(ontology, user.getUsername(), BasePermission.ADMINISTRATION);
        } else {
            if (ontology.getUser() != user) {
                // if user is creator, he keeps access to the ontology
                permissionService.deletePermission(ontology, user.getUsername(), BasePermission.ADMINISTRATION);
            }
        }
        if (projects.stream().anyMatch(project -> userService.listUsers(project).contains(user))) {
            permissionService.addPermission(ontology, user.getUsername(), BasePermission.READ);
        } else {
            if (ontology.getUser() != user) {
                // if user is creator, he keeps access to the ontology
                permissionService.deletePermission(ontology, user.getUsername(), BasePermission.READ);
            }
        }
    }

    protected void afterAdd(CytomineDomain domain, CommandResponse response) {
        permissionService.addPermission(domain, currentUserService.getCurrentUsername(), BasePermission.ADMINISTRATION);
    }

    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        Ontology ontology = (Ontology) domain;
        if (ontology != null && ontology.getName() != null) {
            if (ontologyRepository.findByName(ontology.getName()).stream()
                .anyMatch(x -> !Objects.equals(x.getId(), ontology.getId()))) {
                throw new AlreadyExistException("Ontology " + ontology.getName() + " already exist!");
            }
        }
    }

    public void deleteDependencies(CytomineDomain domain, Transaction transaction, Task task) {
        deleteDependentProject((Ontology) domain, transaction, task);
    }

    private void deleteDependentProject(Ontology ontology, Transaction transaction, Task task) {
        if (!projectRepository.findAllByOntologyId(ontology.getId()).isEmpty()) {
            throw new ConstraintException("Ontology is linked with project. Cannot delete ontology!");
        }
    }

    public OntologyExport export(Ontology ontology) {
        User currentUser = currentUserService.getCurrentUser();
        Page<TermResponse> terms = termHttpContract.findTermsByOntology(
            ontology.getId(),
            currentUser.getId(),
            Pageable.unpaged()
        );
        return new OntologyExport(
            ontology.getName(),
            terms.getContent().stream().map(TermSummary::from).toList()
        );
    }
}
