package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record UserResponse(long id,
                           String username,
                           String email,
                           Optional<String> lastname,
                           Optional<String> firstname,
                           Optional<String> language,
                           Optional<LocalDateTime> updated,
                           Optional<LocalDateTime> deleted,
                           LocalDateTime created) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.USER;
    }
}
