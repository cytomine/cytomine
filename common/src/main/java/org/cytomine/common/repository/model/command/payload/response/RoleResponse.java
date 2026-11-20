package org.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.common.repository.model.command.DataType;

public record RoleResponse(
    long id,
    String authority,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements ApplyCommandResponse {
    public RoleResponse {
        if (deleted == null) {
            deleted = Optional.empty();
        }
    }


    @Override
    public DataType getDataType() {
        return DataType.ROLE;
    }
}
