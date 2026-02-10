# Configuration

This page provides some features that can be configured on Cytomine.

## Configure LS-AAI Identity Broker

Cytomine uses keycloak as IAM and also as a broker to negotiate with LS-AAI to authenticate users using `authorization_code` flow and go to <https://127.0.0.1/iam/realms/cytomine/.well-known/openid-configuration> for the metadata, for configuration follow steps below:

### A. Configure the broker

1. Access keycloak admin console <https://127.0.0.1/iam/admin> and authenticate using the default `admin` user and find the password for it in `cytomine.yaml` under `KEYCLOAK_ADMIN_PASSWORD` and notice this is not the cytomine admin.
2. Click `Identity Providers` in the menu.
3. Select `Cytomine` realm from the upper left corner
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
export PATH=$PATH:/usr/local/go/bin
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

Using the Python client in your locally cloned Cytomine directory, you can import the datasets with the following command:

```bash
python import_datasets.py --cytomine_core_host <cytomine-core-host> --cytomine_pims_host <cytomine-pims-host> --private_key <private-key> --public_key <public-key>
```

where `<cytomine-core-host>` and `<cytomine-pims-host>` is your Cytomine host (if local installation: <http://127.0.0.1/>), `<public-key>` and `<private-key>` are your public and private keys (you can get them in the Cytomine Web-UI in Account).

::: danger
Several factors can slow down the importation:

- You are using `sdafs`, it is currently slow the first time the data are loaded
- You have a large amount of files on your filesystem
:::
