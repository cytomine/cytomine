package be.cytomine.security.current;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import lombok.Data;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.security.User;

@Data
public class PartialCurrentUser implements CurrentUser {

    String username;

    @Override
    public boolean isFullObjectProvided() {
        return false;
    }

    @Override
    public boolean isUsernameProvided() {
        return username != null;
    }

    @Override
    public UserResponse getUser() {
        User user = new User();
        user.setUsername(username);
        return new UserResponse(-1, username, "", Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), false, Optional.empty(), Optional.empty(), Optional.empty(), LocalDateTime.now(),
            Optional.empty(), Optional.empty(), Set.of());
    }
}
