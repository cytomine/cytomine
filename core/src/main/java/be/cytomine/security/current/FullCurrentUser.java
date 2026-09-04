package be.cytomine.security.current;

import lombok.Data;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;

@Data
public class FullCurrentUser implements CurrentUser {

    private UserResponse user;

    @Override
    public boolean isFullObjectProvided() {
        return true;
    }

    @Override
    public boolean isUsernameProvided() {
        return true;
    }
}
