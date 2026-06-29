package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.HasInstantCUD;
import be.cytomine.common.repository.model.command.DataType;

public record OntologyResponse(
    String name,
    long id,
    Set<TermResponse> terms,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted,
    long user // TODO rename `user` to `userId` ? Needs work on the front then.
) implements ApplyCommandResponse, HasInstantCUD {
    public OntologyResponse {
        if (terms == null) {
            terms = new HashSet<>();
        }
        if (deleted == null) {
            deleted = Optional.empty();
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.ONTOLOGY;
    }
}
