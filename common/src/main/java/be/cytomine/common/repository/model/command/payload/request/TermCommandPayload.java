package be.cytomine.common.repository.model.command.payload.request;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record TermCommandPayload(
    Optional<Long> parent,
    long id,
    String name,
    String color,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted,
    String comment,
    long ontology
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return ontology;
    }
}
