package be.cytomine.dto;

import java.util.List;

public record Account(
    String username,
    String lastName,
    String firstName,
    String password,
    String email,
    boolean emailVerified,
    boolean isDeveloper,
    String locale,
    List<String> roles
) {
    public Account {
        if (locale == null) {
            locale = "";
        }
    }

}
