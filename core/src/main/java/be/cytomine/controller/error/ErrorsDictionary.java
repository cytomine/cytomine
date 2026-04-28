package be.cytomine.controller.error;

import java.util.HashMap;
import java.util.Map;

public class ErrorsDictionary {
    private static final Map<ErrorCode, String> errors = new HashMap<>();

    static {
        errors.put(ErrorCode.CORE_CUSTOM_ATTRIBUTES_NOT_SET, "custom attributes not set properly in IAM");
        errors.put(ErrorCode.CORE_ACCOUNT_ALREADY_EXISTS,
            "account already exists consider update or use different values");
        errors.put(ErrorCode.CORE_INVALID_ACCOUNT, "some account properties not valid");
        errors.put(ErrorCode.CORE_INVALID_REFERENCE, "reference is not UUID");
        errors.put(ErrorCode.CORE_USERNAME_UPDATE_NOT_ALLOWED, "updating username is not allowed");
        errors.put(ErrorCode.CORE_IAM_UNKNOWN_DELETE_ERROR, "unknown error from IAM account not deleted");
        errors.put(ErrorCode.CORE_IAM_UNKNOWN_CREATE_ERROR, "unknown error from IAM account not created");
        errors.put(ErrorCode.CORE_INVALID_ROLES,
            "invalid roles, check realm [cytomine] client [core] roles in IAM service");
        errors.put(ErrorCode.CORE_INVALID_CLIENT, "invalid client-id, check client-id [core] is configured in core");
        errors.put(ErrorCode.CORE_ROLES_NOT_ASSIGNED_TO_ACCOUNT, "failed to assign roles to user");
        errors.put(ErrorCode.CORE_REALM_NOT_FOUND, "invalid realm, check realm [cytomine] is configured in core");
        errors.put(ErrorCode.CORE_IAM_NOT_REACHABLE, "core can not reach IAM microservice");
        errors.put(ErrorCode.CORE_ACCOUNT_NOT_FOUND, "user not found in IAM");
    }

    public static Error get(ErrorCode code, Map<String, String> details) {
        return new Error(errors.get(code), code, details);
    }
}
