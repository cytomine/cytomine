package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record UserRoleResponse(long id,
                               long userId,
                               long roleId,
                               LocalDateTime created,
                               Optional<LocalDateTime> updated,
                               Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.USER_ROLE;
    }
}
