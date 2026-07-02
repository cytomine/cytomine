package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record StorageResponse(
    long id,
    long userId,
    String name,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) implements ApplyCommandResponse {
    @Override public DataType getDataType() {
        return DataType.STORAGE;
    }
}
