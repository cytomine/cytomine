package be.cytomine.controller.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.OntologyHttpContract;

@RequiredArgsConstructor
@RestController
public class OntologyController {
    private final OntologyHttpContract ontologyServiceClient;
}
