package org.cytomine.common.authorizationapi.http;

import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(PermissionsHttpContract.ROOT_PATH)
public interface PermissionsHttpContract {
    String ROOT_PATH = "/authorize";

    @PostMapping
    AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest);
}
