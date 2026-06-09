package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class OntologyController {

    public static final String UNABLE_TO_FIND_ONTOLOGY = "Unable to find ontology with id: %s";

    private final OntologyHttpContract ontologyHttpContract;
    private final CurrentUserService currentUserService;

    @GetMapping("/ontology/{id}.json")
    public OntologyResponse show(@PathVariable long id) {
        log.debug("REST request to get Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return ontologyHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }

    @PostMapping("/ontology.json")
    public Optional<HttpCommandResponse> add(@RequestBody CreateOntology createOntology) {
        log.debug("REST request to save Ontology");
        long userId = currentUserService.getCurrentUser().getId();
        return ontologyHttpContract.create(userId, createOntology);
    }

    @PutMapping("/ontology/{id}.json")
    public HttpCommandResponse edit(@PathVariable long id, @RequestBody UpdateOntology updateOntology) {
        log.debug("REST request to edit Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return ontologyHttpContract.update(id, userId, updateOntology)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }

    @DeleteMapping("/ontology/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return ontologyHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }
}
