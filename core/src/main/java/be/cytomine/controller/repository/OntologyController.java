package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.OntologyLight;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.dto.ontology.OntologyExport;
import be.cytomine.dto.ontology.TermSummary;
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
    private final TermHttpContract termHttpContract;
    private final CurrentUserService currentUserService;
    private final PageMapper pageMapper;

    @GetMapping("/ontology/{id}.json")
    public OntologyResponse show(@PathVariable long id) {
        log.debug("REST request to get Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().id();
        return ontologyHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }

    @GetMapping("/ontology_light.json")
    public CollectionResponse<OntologyLight> getAllLightForUser(Pageable pageable) {
        log.debug("REST request to get Ontologies");
        long userId = currentUserService.getCurrentUser().id();
        return pageMapper.toCollectionResponse(ontologyHttpContract.getAllLightForUser(userId, pageable));
    }

    @GetMapping("/ontology.json")
    public CollectionResponse<OntologyResponse> getAll(Pageable pageable) {
        log.debug("REST request to get Ontologies");
        long userId = currentUserService.getCurrentUser().id();
        return pageMapper.toCollectionResponse(ontologyHttpContract.getAllForUser(userId, pageable));
    }

    @GetMapping(value = "/ontology/{id}/export")
    public ResponseEntity<OntologyExport> export(@PathVariable Long id) {
        log.debug("GET /ontology/{}/export", id);

        Long userId = currentUserService.getCurrentUser().id();
        OntologyLight ontology = ontologyHttpContract.getLight(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));

        Page<TermResponse> terms = termHttpContract.findTermsByOntology(id, userId, Pageable.unpaged());
        OntologyExport export =
            new OntologyExport(ontology.name(), terms.getContent().stream().map(TermSummary::from).toList());

        String filename = ontology.name() + ".json";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .body(export);
    }

    @PostMapping("/ontology.json")
    public Optional<HttpCommandResponse> add(@RequestBody CreateOntology createOntology) {
        log.debug("REST request to save Ontology");
        long userId = currentUserService.getCurrentUser().id();
        return ontologyHttpContract.create(userId, createOntology);
    }

    @PutMapping("/ontology/{id}.json")
    public HttpCommandResponse edit(@PathVariable long id, @RequestBody UpdateOntology updateOntology) {
        log.debug("REST request to edit Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().id();
        return ontologyHttpContract.update(id, userId, updateOntology)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }

    @DeleteMapping("/ontology/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete Ontology : {}", id);
        long userId = currentUserService.getCurrentUser().id();
        return ontologyHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ONTOLOGY, id)));
    }
}
