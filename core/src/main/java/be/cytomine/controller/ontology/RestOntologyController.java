package be.cytomine.controller.ontology;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.dto.ontology.OntologyExport;
import be.cytomine.service.ontology.OntologyService;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestOntologyController extends RestCytomineController {

    private final OntologyService ontologyService;

    /**
     * List all ontology visible for the current user For each ontology, print the terms tree
     */
    @GetMapping("/ontology.json")
    public ResponseEntity<String> list(@RequestParam Map<String, String> allParams) {
        log.debug("REST request to list ontologies");
        boolean light = allParams.containsKey("light") && Boolean.parseBoolean(allParams.get("light"));
        return responseSuccess(light ? ontologyService.listLight() : ontologyService.list());
    }

    @GetMapping(value = "/ontology/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OntologyExport> export(@PathVariable Long id) {
        log.debug("GET /ontology/{}/export", id);

        Ontology ontology = ontologyService.find(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ontology not found with id: " + id));

        OntologyExport export = ontologyService.export(ontology);

        String filename = ontology.getName() + ".json";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .body(export);
    }
}
