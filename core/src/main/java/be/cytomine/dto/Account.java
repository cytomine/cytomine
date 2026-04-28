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
    String userLocale,
    List<String> roles
) {
    // Compact constructor to handle default values
    public Account {
        if (userLocale == null) {
            userLocale = "";
        }
    }

}
