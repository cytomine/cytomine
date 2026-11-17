package org.cytomine.common.authorizationapi.http;

import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.service.PermissionsServiceV2;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(PermissionsHttpContract.ROOT_PATH)
public interface PermissionsHttpContract extends PermissionsServiceV2 {
    String ROOT_PATH = "/authorize";

    @PostExchange
    AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest);
}
