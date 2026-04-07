package be.cytomine.common.repository.model.command.payload.term;

import java.util.Optional;

public record TermCommandPayload(Optional<Long> parent, long id, String name, String color, String created,
                                 String updated, String comment, long ontology) {
}
