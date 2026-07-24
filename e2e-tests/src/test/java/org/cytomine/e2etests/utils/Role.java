package org.cytomine.e2etests.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "Guest"),
    USER("ROLE_USER", "User"),
    ADMIN("ROLE_ADMIN", "Admin");

    private final String value;
    private final String label;
}
