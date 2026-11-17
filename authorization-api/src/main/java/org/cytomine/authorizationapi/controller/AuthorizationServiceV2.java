package org.cytomine.authorizationapi.controller;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.http.PermissionsHttpContract;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.beans.authorization.internal.AuthorizationInternalResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController(PermissionsHttpContract.ROOT_PATH)
public class AuthorizationServiceV2 implements PermissionsHttpContract {
    private final AuthorizationInternalResolver authorizationInternalResolver;

    @PostMapping
    @Override
    public AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest) {
        return authorizationInternalResolver.authorize(authorizationRequest);
    }
}
