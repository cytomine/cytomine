package be.cytomine.service.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.jsonwebtoken.lang.Strings;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.controller.error.ErrorBuilder;
import be.cytomine.controller.error.ErrorCode;
import be.cytomine.dto.Account;
import be.cytomine.exceptions.UserManagementException;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.utils.Validation;
import be.cytomine.service.utils.ValidationFor;

import static be.cytomine.service.utils.IamRepresentationUtil.getAccountRepresentation;
import static be.cytomine.service.utils.IamRepresentationUtil.setCustomAttributes;
import static be.cytomine.service.utils.IamRepresentationUtil.setPermanentPassword;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    @Autowired
    Keycloak keycloak;

    @Value("${keycloak-client.target.client-id}")
    String clientId;

    @Value("${keycloak-client.target.realm}")
    String realm;

    private UserRepository userRepository;

    public void createAccount(Account account) throws UserManagementException {

        log.info("Creating account for user {}", account.getUsername());
        // validate account
        Validation validation = validateAccount(account, ValidationFor.CREATE);
        if (!validation.isOk()) {
            throw new UserManagementException("account data is invalid", 400);
        }

        UserRepresentation user = getAccountRepresentation(account);
        // Get realm
        RealmResource realmResource = keycloak.realm(realm);

        UsersResource usersResource;
        Response response;
        try {
            usersResource = realmResource.users();
            response = usersResource.create(user);
            log.info("created account for user {} in IAM", account.getUsername());
        } catch (ServerErrorException e) {
            throw new UserManagementException(ErrorCode.CORE_IAM_UNKNOWN_CREATE_ERROR.toString(), 500);
        }

        // in case account wasn't successfully created in IAM
        if (response.getStatus() == 404) {
            log.error("Realm {} not found", realm);
            throw new UserManagementException(ErrorCode.CORE_REALM_NOT_FOUND.toString(), 500);
        } else if (response.getStatus() == 409) {
            log.error("Account {} already exists", account.getUsername());
            throw new UserManagementException(ErrorCode.CORE_ACCOUNT_ALREADY_EXISTS.toString(),
                HttpStatus.CONFLICT.value());
        } else if (response.getStatus() != 201) {
            log.error("Failed to create account {} for [{}] from IAM", account.getUsername(),
                response.getStatusInfo().getReasonPhrase());
            throw new UserManagementException(ErrorCode.CORE_IAM_UNKNOWN_CREATE_ERROR.toString(), 500);
        }
        // Get client
        ClientRepresentation client = getClientRepresentation(realmResource, usersResource, user);

        List<RoleRepresentation> listOfRoles =
            getRoleRepresentations(account, realmResource, client, usersResource, user);

        // Assign client level roles to user
        try {
            UserResource userResource =
                usersResource.get(CreatedResponseUtil.getCreatedId(response));
            userResource.roles().clientLevel(client.getId()).add(listOfRoles);
            log.info("assigned roles for user {}", account.getUsername());
        } catch (WebApplicationException e) {
            usersResource.delete(user.getId());
            throw new UserManagementException(ErrorCode.CORE_ROLES_NOT_ASSIGNED_TO_ACCOUNT.toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
        log.info("Created account for user {} successfully", account.getUsername());
    }

    private List<RoleRepresentation> getRoleRepresentations(Account account,
                                                            RealmResource realmResource,
                                                            ClientRepresentation client,
                                                            UsersResource usersResource,
                                                            UserRepresentation user) {
        log.info("Retrieving roles for user {}", account.getUsername());
        List<RoleRepresentation> listOfRoles;
        try {
            // Get client level roles (requires view-clients roles in IAM)
            listOfRoles = account.getRoles().stream()
                .map(role -> realmResource.clients()
                    .get(client.getId())
                    .roles()
                    .get(role)
                    .toRepresentation())
                .collect(Collectors.toList());
        } catch (NotFoundException e) {
            log.error("roles {} not found and deleting partially created account",
                account.getRoles());
            deletePartiallyCreatedAccount(usersResource, user);
            throw new UserManagementException(400, ErrorCode.CORE_INVALID_ROLES);
        }
        log.info("Retrieved roles for user {}", account.getUsername());
        return listOfRoles;
    }

    private ClientRepresentation getClientRepresentation(RealmResource realmResource,
                                                         UsersResource usersResource,
                                                         UserRepresentation user) {
        log.info("Retrieving clients");
        ClientRepresentation client;
        try {
            client = realmResource.clients().findByClientId(clientId).get(0);
        } catch (IndexOutOfBoundsException e) {
            log.error("client {} not found and deleting partially created account", clientId);
            deletePartiallyCreatedAccount(usersResource, user);
            throw new UserManagementException(500, ErrorCode.CORE_INVALID_CLIENT);
        }
        log.info("Retrieved client {}", client.getClientId());
        return client;
    }

    private ClientRepresentation getClientRepresentation() {
        ClientRepresentation client;
        try {
            client = keycloak.realm(realm).clients().findByClientId(clientId).get(0);
        } catch (IndexOutOfBoundsException e) {
            log.error("client {} not found and deleting partially created account", clientId);
            throw new UserManagementException(500, ErrorCode.CORE_INVALID_CLIENT);
        }
        return client;
    }

    private List<RoleRepresentation> getClientLevelUserRoles(UserRepresentation userRepresentation,
                                                             ClientRepresentation client) {
        return keycloak.realm(realm).users().get(userRepresentation.getId()).roles()
            .clientLevel(client.getId()).listAll();
    }

    private static void deletePartiallyCreatedAccount(UsersResource usersResource,
                                                      UserRepresentation user) {
        UserRepresentation badAccount =
            usersResource.searchByUsername(user.getUsername(), true).get(0);
        usersResource.delete(badAccount.getId());
    }




    private Account setAccount(UserRepresentation userRepresentation, ClientRepresentation client) {
        Account account = new Account();
        account.setReference(userRepresentation.getId());
        account.setFirstName(userRepresentation.getFirstName());
        account.setLastName(userRepresentation.getLastName());
        account.setEmail(userRepresentation.getEmail());
        account.setUsername(userRepresentation.getUsername());
        account.setEmailVerified(userRepresentation.isEmailVerified());
        try {
            if (Optional.ofNullable(userRepresentation.getAttributes()).isPresent()) {
                account.setDeveloper(userRepresentation.getAttributes().get("isDeveloper").get(0)
                    .equalsIgnoreCase("1"));
                account.setUserLocale(userRepresentation.getAttributes().get("user_locale").get(0));
                account.setUserId(
                    Long.valueOf(userRepresentation.getAttributes().get("user_id").get(0)));

            }
        } catch (NullPointerException e) {
            throw new UserManagementException(500,
                ErrorCode.CORE_CUSTOM_ATTRIBUTES_NOT_SET);
        }
        List<RoleRepresentation> roleRepresentations =
            getClientLevelUserRoles(userRepresentation, client);

        account.setRoles(roleRepresentations.stream().map(RoleRepresentation::getName)
            .collect(Collectors.toList()));
        return account;
    }

    public void update(Account account) {
        log.info("Updating account {}", account);
        // validate account
        Validation validation = validateAccount(account, ValidationFor.UPDATE);
        if (!validation.isOk()) {
            throw new UserManagementException("account data is invalid", 400);
        }

        UsersResource users = keycloak.realm(realm).users();
        UserRepresentation userRepresentation = null;
        UserRepresentation userRepresentationBeforeUpdate = null;
        try {
            userRepresentation = users.searchByUsername(account.getUsername(), true).get(0);
            userRepresentationBeforeUpdate =
                userRepresentation; // used to roll back operation in IAM in case of downstream failure
            userRepresentation.setFirstName(account.getFirstName());
            userRepresentation.setLastName(account.getLastName());
            userRepresentation.setEmail(account.getEmail());
            userRepresentation.setUsername(account.getUsername());

            setCustomAttributes(account, userRepresentation);

            // Set password credential
            if (Objects.nonNull(account.getPassword())) {
                userRepresentation.setCredentials(setPermanentPassword(account));
            }

            // update account in IAM
            users.get(userRepresentation.getId()).update(userRepresentation);


        } catch (NotFoundException e) {
            // create the account if it doesn't exist in IAM
            log.info("account {} not found in IAM", account.getUsername());
        } catch (BadRequestException e) {
            log.error("IAM doesn't allow the update of username {} with error {}",
                account.getUsername(),
                e.getMessage());
            throw new UserManagementException(ErrorCode.CORE_USERNAME_UPDATE_NOT_ALLOWED.toString(),
                HttpStatus.BAD_REQUEST.value());
        }

        ClientRepresentation clientRepresentation = getClientRepresentation();
        updateAccountClientLevelRoles(account, users, clientRepresentation, userRepresentation);

        log.info("account updated successfully");
    }


    private void updateAccountClientLevelRoles(Account account, UsersResource users,
                                               ClientRepresentation client,
                                               UserRepresentation userRepresentation) {
        log.info("updating account client level roles to {}", account.getUsername());
        try {
            UserResource userResource = users.get(userRepresentation.getId());
            List<RoleRepresentation> listOfRoles = getAccountRoleRepresentations(account, client);
            List<RoleRepresentation> allRoles =
                userResource.roles().clientLevel(client.getId()).listAll();
            userResource.roles().clientLevel(client.getId()).remove(allRoles);

            if (listOfRoles.isEmpty()) {
                account.getRoles().clear();
            } else {
                userResource.roles().clientLevel(client.getId()).add(listOfRoles);
            }


        } catch (WebApplicationException e) {
            log.error("failed to assign roles to account {}", account.getReference(), e);
            throw new UserManagementException(500,
                ErrorCode.CORE_ROLES_NOT_ASSIGNED_TO_ACCOUNT);
        }
    }

    private List<RoleRepresentation> getAccountRoleRepresentations(Account account,
                                                                   ClientRepresentation client) {
        log.info("getting account client level roles to {}", account.getReference());
        List<RoleRepresentation> listOfRoles;
        try {
            // Get client level roles (requires view-clients roles in IAM)
            listOfRoles = account.getRoles().stream()
                .map(role -> keycloak.realm(realm).clients()
                    .get(client.getId())
                    .roles()
                    .get(role)
                    .toRepresentation())
                .collect(Collectors.toList());
        } catch (NotFoundException e) {
            log.error("account roles {} not found in IAM", account.getRoles(), e);
            throw new UserManagementException(400, ErrorCode.CORE_INVALID_ROLES);
        }
        log.info("account roles retrieved successfully");
        return listOfRoles;
    }

    private Validation validateAccount(Account account, ValidationFor source) {
        log.info("Validating account {}", account.toString());
        Validation validation = new Validation();
        validation.setOk(true);
        Map<String, String> errors = new HashMap<>();
        // validate required properties
        // updating username and password is not allowed but the error will come from IAM as it's
        // impossible to validate the values here
        if (source.equals(ValidationFor.UPDATE)) {
            log.info("Validating account for update");
            if (account.getUsername() == null) {
                validation.setOk(false);
                errors.put("username", "this property is required");
            } else {
                validation.setOk(true);
                log.info("Validated account {} successfully", account.getUsername());
                return validation;
            }
        }

        if (source.equals(ValidationFor.CREATE)) {
            log.info("Validating account for create");
            if (account.getPassword() == null || account.getPassword().isEmpty()) {
                validation.setOk(false);
                errors.put("password", "this property is required");
            }
            if (account.getFirstName() == null || account.getFirstName().isEmpty()) {
                validation.setOk(false);
                errors.put("first_name", "this property is required");
            }
            if (account.getLastName() == null || account.getLastName().isEmpty()) {
                validation.setOk(false);
                errors.put("last_name", "this property is required");
            }
            if (account.getUsername() == null || account.getUsername().isEmpty()) {
                validation.setOk(false);
                errors.put("username", "this property is required");
            }
            if (account.getEmail() == null || account.getEmail().isEmpty()) {
                validation.setOk(false);
                errors.put("email", "this property is required");
            }
            if (account.getUserLocale() == null || account.getUserLocale().isEmpty()) {
                validation.setOk(false);
                errors.put("user_locale", "this property is required");
            } else {
                List<String> cytomineLocales = List.of("en", "es", "nl", "fr", "no");
                if (!cytomineLocales.contains(account.getUserLocale())) {
                    validation.setOk(false);
                    errors.put("user_locale", "unknown locales [" + account.getUserLocale()
                        + "] , allowed roles [en, es, nl, fr, no]");
                }
            }
            if (account.getRoles() == null || account.getRoles().isEmpty()) {
                validation.setOk(false);
                errors.put("roles", "this property is required");
            } else {
                List<String> cytomineRoles = List.of("ADMIN", "USER", "GUEST");
                List<String> invalidRoles =
                    account.getRoles().stream().filter(role -> !cytomineRoles.contains(role))
                        .toList();
                if (!invalidRoles.isEmpty()) {
                    String unknownRoles = Strings.collectionToCommaDelimitedString(invalidRoles);
                    validation.setOk(false);
                    errors.put("roles", "unknown roles [" + unknownRoles
                        + "] , allowed roles [ADMIN, USER, GUEST]");
                } else {
                    validation.setOk(true);
                    log.info("Validated account {} successfully", account.getUsername());
                    return validation;
                }
            }
        }
        log.info("Validated account {} with following errors {}", account.getUsername(), errors);
        validation.setResponseEntity(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorBuilder.build(ErrorCode.CORE_INVALID_ACCOUNT, errors)));
        return validation;
    }

    private Validation validateReference(String reference) {
        log.info("validating account reference {}", reference);
        Validation validation = new Validation();
        validation.setOk(true);
        try {
            UUID.fromString(reference);
        } catch (IllegalArgumentException ex) {
            validation.setOk(false);
            validation.setResponseEntity(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorBuilder.build(ErrorCode.CORE_INVALID_REFERENCE)));
            return validation;
        }
        log.info("Validated account reference {} successfully", reference);
        return validation;
    }
}