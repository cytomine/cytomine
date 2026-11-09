package org.cytomine.resourceserver.security;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.config.security.http.PermissionsHttpContract;
import be.cytomine.common.resourceserver.model.AuthorizationRequest;
import be.cytomine.common.resourceserver.model.AuthorizationResponse;

@RestController("/authorize")
public class AuthorizationService implements PermissionsHttpContract {
    @PostMapping
    @Override
    public AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest) {

    }

}
