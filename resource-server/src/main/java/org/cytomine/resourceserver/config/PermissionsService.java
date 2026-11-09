package org.cytomine.resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PermissionsService {

    private final CytominePermissions cytominePermissions;

}
