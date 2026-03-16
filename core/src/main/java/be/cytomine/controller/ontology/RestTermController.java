package be.cytomine.controller.ontology;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.ontology.OntologyRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.ontology.TermService;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestTermController extends RestCytomineController {

    private final TermService termService;

    private final OntologyRepository ontologyRepository;

    private final ProjectRepository projectRepository;

    @GetMapping("/ontology/{id}/term.json")
    public ResponseEntity<String> listByOntology(@PathVariable Long id) {
        log.debug("REST request to list terms for ontology {}", id);
        return ontologyRepository.findById(id)
                .map( ontology -> responseSuccess(termService.list(ontology)))
                .orElseThrow(() -> new ObjectNotFoundException("Ontology", id));
    }

    @GetMapping("/project/{id}/term.json")
    public ResponseEntity<String> listByProject(@PathVariable Long id) {
        log.debug("REST request to list terms for project {}", id);
        return projectRepository.findById(id)
                .map( ontology -> responseSuccess(termService.list(ontology)))
                .orElseThrow(() -> new ObjectNotFoundException("Ontology", id));
    }
}
