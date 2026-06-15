package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record StorageResponse(
    long id,
    long userId,
    String name,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements ApplyCommandResponse {
    @Override public DataType getDataType() {
        return DataType.STORAGE;
    }
}
