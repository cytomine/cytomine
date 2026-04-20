package be.cytomine.controller.error;

import java.util.HashMap;
import java.util.Map;

public class ErrorsDictionary {
    private static final Map<ErrorCode, Error> errors = new HashMap<>();

    static {
        errors.put(ErrorCode.CORE_CUSTOM_ATTRIBUTES_NOT_SET, new Error("CORE-CUSTOM-ATTRIBUTES-NOT-SET" , "custom attributes not set properly in IAM")); ;
        errors.put(ErrorCode.CORE_ACCOUNT_ALREADY_EXISTS, new Error("CORE-ACCOUNT-ALREADY-EXISTS" , "account already exists consider update or use different values")) ;
        errors.put(ErrorCode.CORE_INVALID_ACCOUNT, new Error("CORE-INVALID-ACCOUNT" , "some account properties not valid"));
        errors.put(ErrorCode.CORE_INVALID_REFERENCE, new Error("CORE-INVALID-REFERENCE" , "reference is not UUID"));
        errors.put(ErrorCode.CORE_USERNAME_UPDATE_NOT_ALLOWED, new Error("CORE-USERNAME-UPDATE-NOT-ALLOWED" , "updating username is not allowed"));
        errors.put(ErrorCode.CORE_IAM_UNKNOWN_DELETE_ERROR, new Error("CORE-IAM-UNKNOWN-DELETE-ERROR" , "unknown error from IAM account not deleted"));
        errors.put(ErrorCode.CORE_IAM_UNKNOWN_CREATE_ERROR, new Error("CORE-IAM-UNKNOWN-CREATE-ERROR" , "unknown error from IAM account not created"));
        errors.put(ErrorCode.CORE_INVALID_ROLES, new Error("CORE-INVALID-ROLES" , "invalid roles, check realm [cytomine] client [core] roles in IAM service"));
        errors.put(ErrorCode.CORE_INVALID_CLIENT, new Error("CORE-INVALID-CLIENT" , "invalid client-id, check client-id [core] is configured in core"));
        errors.put(ErrorCode.CORE_ROLES_NOT_ASSIGNED_TO_ACCOUNT, new Error("CORE-ROLES-NOT-ASSIGNED", "failed to assign roles to user"));
        errors.put(ErrorCode.CORE_REALM_NOT_FOUND, new Error("CORE-IAM-REALM-NOT-FOUND", "invalid realm, check realm [cytomine] is configured in core"));
        errors.put(ErrorCode.CORE_IAM_NOT_REACHABLE, new Error("CORE-IAM-REACHABLE", "core can not reach IAM microservice"));
        errors.put(ErrorCode.CORE_ACCOUNT_NOT_FOUND, new Error("CORE-ACCOUNT-NOT-FOUND", "user not found in IAM"));
    }

    public static Error get(ErrorCode code) {
        return errors.get(code);
    }
}
