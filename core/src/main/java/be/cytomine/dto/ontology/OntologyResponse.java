package be.cytomine.dto.ontology;

import java.util.List;

public record OntologyResponse(Long id, String name, List<TermSummary> terms) {}
