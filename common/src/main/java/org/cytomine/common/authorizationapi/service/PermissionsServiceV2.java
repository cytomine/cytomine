package org.cytomine.common.authorizationapi.service;

import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;

public interface PermissionsServiceV2 {
    AuthorizationResponse authorize(AuthorizationRequest authorizationRequest);
}
