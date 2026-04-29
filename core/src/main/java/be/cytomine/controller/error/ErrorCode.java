package be.cytomine.controller.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
     CORE_CUSTOM_ATTRIBUTES_NOT_SET("custom attributes not set properly in IAM"),
     CORE_ACCOUNT_ALREADY_EXISTS("account already exists consider update or use different values"),
     CORE_INVALID_ACCOUNT("some account properties not valid"),
     CORE_INVALID_REFERENCE("reference is not UUID"),
     CORE_USERNAME_UPDATE_NOT_ALLOWED("updating username is not allowed"),
     CORE_IAM_UNKNOWN_DELETE_ERROR("unknown error from IAM account not deleted"),
     CORE_IAM_UNKNOWN_CREATE_ERROR("unknown error from IAM account not created"),
     CORE_INVALID_ROLES("invalid roles, check realm [cytomine] client [core] roles in IAM service"),
     CORE_INVALID_CLIENT("invalid client-id, check client-id [core] is configured in core"),
     CORE_ROLES_NOT_ASSIGNED_TO_ACCOUNT("failed to assign roles to user"),
     CORE_REALM_NOT_FOUND("invalid realm, check realm [cytomine] is configured in core"),
     CORE_IAM_NOT_REACHABLE("core can not reach IAM microservice"),
     CORE_ACCOUNT_NOT_FOUND("user not found in IAM");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
