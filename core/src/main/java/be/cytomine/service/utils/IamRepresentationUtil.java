package be.cytomine.service.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import be.cytomine.controller.error.ErrorCode;
import be.cytomine.dto.Account;
import be.cytomine.exceptions.UserManagementException;

@Slf4j
public class IamRepresentationUtil {


    public static UserRepresentation getAccountRepresentation(Account account) {
        log.info("Retrieving account {}", account.username());
        UserRepresentation user = new UserRepresentation();
        user.setUsername(account.username());
        user.setFirstName(account.firstName());
        user.setLastName(account.lastName());
        user.setEmail(account.email());
        user.setEmailVerified(account.emailVerified());
        user.setEnabled(true);

        // add custom attributes of user : locale , isDeveloper
        setCustomAttributes(account, user);
        log.info("set custom attributes for {}", account.username());

        // Set password credential
        user.setCredentials(setPermanentPassword(account));
        log.info("set permanent credentials for {}", account.username());
        log.info("retrieved account representation for user {}", user.getUsername());
        return user;
    }

    public static List<CredentialRepresentation> setPermanentPassword(Account account) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false); // permanent password
        credential.setValue(account.password());
        return List.of(credential);
    }

    public static void setCustomAttributes(Account account, UserRepresentation user) {
        if (user.getAttributes() == null) {
            user.setAttributes(new HashMap<>());
        }
        try {
            user.getAttributes().put("isDeveloper", Collections.singletonList(account.isDeveloper() ? "1" : "0"));
            user.getAttributes().put("user_locale", Collections.singletonList(account.userLocale()));

        } catch (NullPointerException e) {
            throw new UserManagementException(500, ErrorCode.CORE_CUSTOM_ATTRIBUTES_NOT_SET);
        }
    }

}
