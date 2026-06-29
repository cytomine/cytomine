package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.DataType;

public record TermResponse(
    long id,
    String name,
    String color,
    long ontologyId,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted,
    Optional<String> comment,
    Set<TermResponse> children
) implements ApplyCommandResponse {
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }
}
