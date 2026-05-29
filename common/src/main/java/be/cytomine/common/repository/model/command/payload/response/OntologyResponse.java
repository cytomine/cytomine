package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record OntologyResponse(String name, long id, LocalDateTime created, LocalDateTime updated,
                               Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.ONTOLOGY;
    }
}
