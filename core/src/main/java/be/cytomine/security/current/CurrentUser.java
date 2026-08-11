package be.cytomine.security.current;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;

public interface CurrentUser {

    boolean isFullObjectProvided();

    boolean isUsernameProvided();

    UserResponse getUser();
}
