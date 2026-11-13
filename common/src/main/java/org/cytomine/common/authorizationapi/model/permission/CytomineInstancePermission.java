package org.cytomine.common.authorizationapi.model.permission;

import java.util.Arrays;
import java.util.Optional;

public enum CytomineInstancePermission implements CytominePermission {
    CAN_CREATE_PROJECT, CAN_SEE_ALL_PROJECTS;

    public static Optional<CytomineInstancePermission> fromString(String text) {
        return Arrays.stream(values())
            .filter(bl -> bl.name().equalsIgnoreCase(text))
            .findFirst();
    }
}
