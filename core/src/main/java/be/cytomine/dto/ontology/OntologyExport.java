package be.cytomine.dto.ontology;

import java.util.List;

public record OntologyExport(String name, List<TermSummary> terms) {}
