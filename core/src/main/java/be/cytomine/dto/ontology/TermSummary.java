package be.cytomine.dto.ontology;

import be.cytomine.common.repository.model.command.payload.response.TermResponse;

public record TermSummary(String name, String color) {
    public static TermSummary from(TermResponse term) {
        return new TermSummary(term.name(), term.color());
    }
}
