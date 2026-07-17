package be.cytomine.security.current;

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
        return new UserResponse();
    }
}
