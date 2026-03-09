package be.cytomine.service.utils;

import be.cytomine.controller.error.ErrorCode;
import be.cytomine.dto.Account;
import be.cytomine.exceptions.UserManagementException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class IAM_RepresentationUtil {


    public static UserRepresentation getAccountRepresentation(Account account) {
        log.info("Retrieving account {}", account.getUsername());
        UserRepresentation user = new UserRepresentation();
        user.setUsername(account.getUsername());
        user.setFirstName(account.getFirstName());
        user.setLastName(account.getLastName());
        user.setEmail(account.getEmail());
        user.setEmailVerified(account.isEmailVerified());
        user.setEnabled(true);

        // add custom attributes of user : locale , isDeveloper
        setCustomAttributes(account, user);
        log.info("set custom attributes for {}", account.getUsername());

        // Set password credential
        user.setCredentials(setPermanentPassword(account));
        log.info("set permanent credentials for {}", account.getUsername());
        log.info("retrieved account representation for user {}", user.getUsername());
        return user;
    }

    public static List<CredentialRepresentation> setPermanentPassword(Account account) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false); // permanent password
        credential.setValue(account.getPassword());
        return List.of(credential);
    }

    public static void setCustomAttributes(Account account, UserRepresentation user) {
        if (user.getAttributes() == null) {
            user.setAttributes(new HashMap<>());
        }
        try {
            user.getAttributes().put("isDeveloper", Collections.singletonList(account.isDeveloper() ? "1" : "0"));
            user.getAttributes().put("user_locale", Collections.singletonList(account.getUserLocale()));
            if(account.getUserId() != null)
                user.getAttributes().put("user_id", Collections.singletonList(account.getUserId().toString()));
        } catch (NullPointerException e) {
            throw new UserManagementException(500, ErrorCode.CORE_CUSTOM_ATTRIBUTES_NOT_SET);
        }
    }

}
