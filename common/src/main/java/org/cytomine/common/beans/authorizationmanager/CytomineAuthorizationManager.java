package org.cytomine.common.beans.authorizationmanager;

import java.util.function.Supplier;

import org.cytomine.common.authorizationapi.model.permission.CytominePermission;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;

// https://docs.spring.io/spring-security/reference/servlet/authorization/index.html
public class CytomineAuthorizationManager implements AuthorizationManager<CytominePermission> {

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication,
        CytominePermission object) {
        return null;
    }
}
