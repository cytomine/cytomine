package be.cytomine.security.current;

import lombok.Data;

import be.cytomine.domain.security.User;

@Data
public class FullCurrentUser implements CurrentUser {

    private User user;

    @Override
    public boolean isFullObjectProvided() {
        return true;
    }

    @Override
    public boolean isUsernameProvided() {
        return true;
    }
}
