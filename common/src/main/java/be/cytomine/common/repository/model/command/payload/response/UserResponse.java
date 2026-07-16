package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.DataType;

public record UserResponse(long id, String username, String email, Optional<String> name, Optional<String> lastname,
                           Optional<String> firstname, Optional<String> language,
                           // we'll change that to developer later.
                           boolean isDeveloper,
                           //
                           Optional<String> origin, Optional<LocalDateTime> updated, Optional<LocalDateTime> deleted,
                           LocalDateTime created, Optional<String> privateKey, Optional<String> publicKey,
                           Set<RoleResponse> roles) implements ApplyCommandResponse {
    public UserResponse {
        if (roles == null) {
            roles = new HashSet<>();
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.USER;
    }
}
