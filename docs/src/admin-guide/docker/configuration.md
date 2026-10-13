# Configuration

This page provides some features that can be configured on Cytomine.

## Configure LS-AAI Identity Broker

Cytomine uses keycloak as IAM and also as a broker to negotiate with LS-AAI to authenticate users using `authorization_code` flow and go to <http://127.0.0.1:8100/iam/realms/cytomine/.well-known/openid-configuration> for the metadata, for configuration follow steps below:

### A. Configure the broker

1. Access keycloak admin console <http://127.0.0.1:8100/iam/admin> and authenticate using the default `admin` user and find the password for it in `cytomine.yaml` under `KEYCLOAK_ADMIN_PASSWORD` and notice this is not the cytomine admin.
2. Click `Identity Providers` in the menu.
3. Select `Cytomine` realm from the upper left corner dropdown menu
4. From the `Add provider` list, select `OpenID Connect v1.0` or `keycloak openID connect`.
5. `Redirect URI` is prefilled
6. Enter display name as `LS_AAI`
7. Enter this link `https://login.aai.lifescience-ri.eu/oidc/.well-known/openid-configuration` in `Discovery Endpoint` for LS-AAI OIDC metadata
8. In `client authentication` select `Client secret set as basic auth`
9. Contact cytomine team at ULiège to get the `client ID` and `client secret` , click `contact us` button below
10. Click save
11. scroll down and click `Advanced` and toggle `Backchannel Logout` to `ON`
12. click save again

### B. Map claims to roles

The following config assigns the role `admin` to all external users and this is mandatory.

1. Go to `Mappers` tab click on `add mapper`
2. Enter `AdminMapper` for the mapper and keep `sync mode override` as inherit
3. Select `Hardcoded Role Mapper`
4. Click `Select Role` then filter by client and From client `core` select `ADMIN` role
5. Click save

once steps above are followed a new button appears in the login form to start the authentication process for users coming from other organizations.

## Configure LTI 1.3 Identity Broker

Cytomine's IAM service embeds a custom Keycloak Identity Provider (SPI) that lets a Learning Management System (LMS) launch a Keycloak-protected tool through **LTI 1.3**. In this setup the LMS is the LTI **Platform** and Keycloak is the LTI **Tool**. After the launch, Keycloak verifies the signed `id_token`, provisions or links the user, and assigns a Cytomine core role. The provider is compatible with any LMS that supports LTI 1.3, e.g. Moodle, Canvas, Sakai or Blackboard.

The provider shows up in the Admin Console as **`LTI 1.3`** (provider id `lti-1p3`) under `Identity Providers`.

### A. Create the tool client in Keycloak

1. Access the Keycloak admin console <http://127.0.0.1:8100/iam/admin> and authenticate using the default `admin` user (password available in `cytomine.yaml` under `KEYCLOAK_ADMIN_PASSWORD` — note this is not the Cytomine admin).
2. Click `Clients` and make sure the `Cytomine` realm is selected from the dropdown in the upper left corner.
3. Click `Create client` and use a `Client ID` that represents the protected tool, e.g. `lti-tool`.
4. In `Valid redirect URIs`, add the target link URI(s) where the user should land after the broker flow, e.g. `http://127.0.0.1/*`.
5. Save. This `Client ID` is the **Keycloak Client ID** used in step C.

### B. Register the tool with your LMS

Give the LMS the tool-facing endpoints:

| Value | URL |
|---|---|
| OIDC Login Initiation URL (`Initiate login URL`) | <http://<keycloak-host>:8100/iam/realms/cytomine/lti/login-init> |
| Redirect URI(s) (broker callback) | <http://<keycloak-host>:8100/iam/realms/cytomine/broker/<alias>/endpoint> |
| Target link URI (`Tool URL`) | the Cytomine Web-UI address, e.g. <http://127.0.0.1/> |

The `<alias>` must match the alias of the identity provider created in step C.

**Example with Moodle:**

1. In Moodle go to `Site administration` → `Plugins` → `Activity modules` → `External tool` → `Manage tools`.
2. Click `Configure a tool manually` and fill in:
   - `Tool name`: e.g. `Cytomine`
   - `Tool URL`: the target link URI, e.g. `http://127.0.0.1/`
   - `LTI version`: **LTI 1.3**
   - `Initiate login URL`: the login initiation URL from the table above
   - `Redirection URI(s)`: the broker callback URL from the table above
   - leave `Deep linking` / `Content selection` / `Accept grades` off
3. Save, then open `View configuration details`: Moodle exposes its platform values (`Platform ID`, `Client ID`, `Deployment ID`, `Public keyset URL`, `Access token URL`, `Authentication request URL`) that you will copy in step C.

For any other LMS, register the same tool-facing values; the LMS then exposes its own platform issuer, client id, deployment id and endpoints.

### C. Configure the LTI 1.3 Identity Provider in Keycloak

1. Click `Identity Providers` in the menu.
2. From the `Add provider` list, select **`LTI 1.3`**.
3. Enter an `Alias`, e.g. `lti-moodle`. It must match the `<alias>` used in the Redirect URI from step B.
4. Fill the configuration using the values from your LMS:

| Config field | Set to |
|---|---|
| Platform Issuer | the LMS platform issuer (`Platform ID` / `iss` claim) |
| Tool Client ID | the `Client ID` the LMS assigned to the tool (`aud` claim) |
| Allowed Deployment IDs | the `Deployment ID`(s) the LMS sends (`deployment_id` claim), comma-separated |
| Keycloak Client ID (your tool) | the client created in step A, e.g. `lti-tool` |
| Platform Authorization Endpoint | the LMS authentication request URL |
| Platform JWKS URL | the LMS public keyset URL |
| Subject Claim (advanced) | leave blank (defaults to `sub`) |

5. Click `Save`.

### D. Map LTI roles to Cytomine roles

1. On the identity provider, open the `Mappers` tab and click `Add mapper`.
2. Select the **`LTI Core Roles`** mapper.
3. Optionally adjust the three comma-separated LTI-role lists (values follow the IMS membership vocab, e.g. `Instructor`, `TeachingAssistant`, `Learner`, `Student`):
   - `LTI roles mapped to ADMIN` — empty by default: ADMIN is not granted from LTI launches unless explicitly configured
   - `LTI roles mapped to USER` — default `Instructor,TeachingAssistant`
   - `LTI roles mapped to GUEST` — default `Student,Learner`
4. Click `Save`.

Any launch role that is not matched falls back to `GUEST`. The mapper grants the corresponding role on the `core` client and removes a previously granted ADMIN/USER/GUEST role when the launch role changes.

### E. Values that must match

- The `<alias>` in the LMS `Redirection URI(s)` (`.../broker/<alias>/endpoint`) equals the Keycloak identity provider alias.
- `Platform Issuer` exactly matches the `iss` claim the LMS signs (mind trailing slashes).
- `Tool Client ID` equals the `aud` claim / `Client ID` assigned by the LMS.
- `Allowed Deployment IDs` contains the `deployment_id` claim sent by the LMS.
- The Keycloak tool client's `Valid redirect URIs` (step A) include the target link URI.
- The LMS must be reachable from the IAM container, and vice versa. For local development the IAM service is configured to reach the host via `host.docker.internal` / `localhost` and to skip TLS verification, so a self-signed LMS certificate is tolerated.

### F. Launch the tool

In the LMS, add the tool as an activity (e.g. an `External tool` activity) and open it. The browser is redirected to the login-init endpoint, then to the LMS authorization endpoint, and the LMS posts the signed `id_token` back to the broker callback. Keycloak verifies the signature against the LMS JWKS, validates `iss`, `aud`, `deployment_id`, `nonce`, `exp` and `iat`, creates or links the user, applies the role mapper, and finally redirects the browser to the target link URI (the Cytomine Web-UI).

## Datasets Importation

::: tip
**Importing** does not create explicit copies or duplication of files, only database entries and symbolic links are created.
:::

This section outlines the procedure for importing datasets, which are formatted according to the [BP Dataset File Structure Standard](https://bp.nbis.se/datasets/submission/preparation-guide.html).

### Prerequisites

- [Cytomine Python client](https://github.com/cytomine/cytomine/tree/main/clients/python) (v3.1.0+ recommended)
- [sdafs](https://github.com/NBISweden/sdafs) (optional)

::: tip
`sdafs` is optional and only needed if you do not have the datasets on your filesystem.

The complete installation guide is available in the [sdafs repository](https://github.com/NBISweden/sdafs?tab=readme-ov-file#sdafs).

1. Install go (<https://go.dev/doc/install>)

2. Add the following line to your `$HOME/.profile` or `/etc/profile` (for a system-wide installation):
```bash
export PATH=$PATH:/usr/local/go/bin:$HOME/go/bin
```

3. Install `sdafs`
```bash
go install github.com/NBISweden/sdafs/cmd/sdafs@v0.0.23
```
Where `v0.0.23` is the version you want to install

4. Follow `sdafs` [usage](https://github.com/NBISweden/sdafs?tab=readme-ov-file#usage) documentation to download credentials.

5. Depending on the OS, edit `user_allow_other` variable in `/etc/fuse.conf` as explained in `sdafs` [permissions](https://github.com/NBISweden/sdafs?tab=readme-ov-file#permissions) section.

6. Create a folder to host the mount point
```bash
mkdir bp-datasets
```

7. Mount `sdafs` with the downloaded credentials to the created folder
```bash
sdafs --open --credentialsfile s3cmd-download.conf bp-datasets
```

> The command is from the `sdafs` [usage](https://github.com/NBISweden/sdafs?tab=readme-ov-file#usage) section.
:::

- The structure of the imported directory should follows:

```
root
|--- {DATASET_UUID}
|    |--- DATASET_{IDENTIFIER}
|         |--- METADATA
|         |    |--- dataset.xml
|         |    |--- policy.xml
|         |    |--- image.xml
|         |    |--- annotation.xml
|         |    |--- observation.xml
|         |    |--- observer.xml
|         |    |--- sample.xml
|         |    |--- staining.xml
|         |
|         |--- IMAGES
|         |    |--- IMAGE_{IDENTIFIER}
|         |    |    |--- *.dcm files of an Image
|         |    |--- IMAGE_{IDENTIFIER}
|         |    |    |--- *.dcm files of an Image
|         |
|         |--- ANNOTATIONS
|         |    |--- *.geojson
```

- The `METADATA` and `ANNOTATIONS` directories are optional.

::: tip
If you’re working with `sdafs`, the directories are already organised in the structure shown above.
:::

### [Optional] Configuration

::: tip
This configuration is optional if you planned to use the default location `./cytomine/data/dataset` for importing your datasets.

If you are using `sdafs`, set this path to the mounted folder (e.g., `bp-datasets` as configured in the previous step).
:::

Create `.env` in the cytomine folder and add the path to your dataset folder:

```bash
IMPORT_PATH=<path-to-dataset-folder>
```

Where `<path-to-dataset-folder>` is the path to your dataset folder.

If no `.env` file was provided, the default location is `./cytomine/data/dataset`

::: danger
This step should be done before the `docker compose up -d` command in the Cytomine installation guide.
:::

### Usage

Using the Python client in your locally cloned Cytomine directory, you can import the datasets with the following command executed from the root of the repository:

```bash
python ./clients/python/examples/import_datasets.py --cytomine_core_host <cytomine-core-host> --cytomine_pims_host <cytomine-pims-host> --private_key <private-key> --public_key <public-key>
```

where `<cytomine-core-host>` (if local installation: <http://127.0.0.1:8080>) and `<cytomine-pims-host>` (if local installation: <http://127.0.0.1:5001>) are the host address with port of `core` and `pims`, `<public-key>` and `<private-key>` are your public and private keys (you can get them in the Cytomine Web-UI in Account).

::: danger
Several factors can slow down the importation:

- You are using `sdafs`, it is currently slow the first time the data are loaded
- You have a large amount of files on your filesystem
:::
