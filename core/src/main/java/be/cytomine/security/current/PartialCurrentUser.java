package be.cytomine.security.current;

import lombok.Data;

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
    public User getUser() {
        User user = new User();
        user.setUsername(username);
        return user;
    }
}
