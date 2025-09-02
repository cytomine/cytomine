# Configuration

## Configure LS-AAI Identity Broker

Cytomine uses keycloak as IAM and also as a broker to negotiate with LS-AAI to authenticate users using `authorization_code` flow and go to <https://127.0.0.1/iam/realms/cytomine/.well-known/openid-configuration> for the metadata, for configuration follow steps below:

### A. Configure the broker

1. Access keycloak admin console <https://127.0.0.1/iam/admin> and authenticate using the default `admin` user and find the password for it in `cytomine.yml` under `KEYCLOAK_ADMIN_PASSWORD` and notice this is not the cytomine admin.
2. Click `Identity Providers` in the menu.
3. From the `Add provider` list, select `OpenID Connect v1.0` or `keycloak openID connect`.
4. `Redirect URI` is prefilled
5. Enter display name as `LS_AAI`
6. Enter this link `https://login.aai.lifescience-ri.eu/oidc/.well-known/openid-configuration` in `Discovery Endpoint` for LS-AAI OIDC metadata
7. In `client authentication` select `Client secret set as basic auth`
8. Contact cytomine team at Uliege to get the `client ID` and `client secret` , click `contact us` button below
9. Click save

### B. Map claims to roles

The following config assigns the role `admin` to all external users and this is mandatory.

1. Go to `Mappers` tab click on `add mapper`
2. Enter `name` for the mapper and keep `sync mode override` as inherit
3. Select `Hardcoded Role Mapper`
4. From client `core` select `ADMIN` role
5. Click save

once steps above are followed a new button appears in the login form to start the authentication process for users coming from other organizations.
