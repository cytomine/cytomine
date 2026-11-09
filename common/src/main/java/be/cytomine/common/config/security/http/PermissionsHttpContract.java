package be.cytomine.common.config.security.http;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;

import be.cytomine.common.resourceserver.model.AuthorizationRequest;
import be.cytomine.common.resourceserver.model.AuthorizationResponse;

@HttpExchange(PermissionsHttpContract.ROOT_PATH)

public interface PermissionsHttpContract {
    String ROOT_PATH = "/authorize";

    @PostMapping
    AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest);
}
