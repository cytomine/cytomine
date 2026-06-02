package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.DataType;

public record OntologyResponse(String name, long id, Set<TermResponse> terms, LocalDateTime created,
                               LocalDateTime updated, Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    public OntologyResponse {
        if (terms == null) {
            terms = new HashSet<>();
        }
    }


    @Override
    public DataType getDataType() {
        return DataType.ONTOLOGY;
    }
}
