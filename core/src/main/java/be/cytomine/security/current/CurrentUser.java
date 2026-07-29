package be.cytomine.security.current;

import be.cytomine.domain.security.User;

public interface CurrentUser {

    boolean isFullObjectProvided();

    boolean isUsernameProvided();

    User getUser();
}
