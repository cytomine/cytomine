# Install Cytomine on Kubernetes

::: tip
If you experiment any issues, please describe your problem precisely [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

::: danger
This Helm chart is under active development and may be unstable. Use it at your own risk!
:::

## Prerequisites

- [Git](https://git-scm.com/) (v2.0+ recommended)
- [Helm](https://helm.sh/) (v3.0+ recommended)
- A Kubernetes cluster

## Installation

1. Clone the cytomine repository:

    ```bash
    git clone https://github.com/cytomine/cytomine.git
    cd cytomine
    ```

2. Configure the mandatory values in `cytomine/helm/charts/cytomine/values.yaml`:

    ```yaml
    # Your domain name
    global:
      domainName: null
    ```

    All other values can use the default settings.

3. Install the Cytomine Helm chart with a release name `cytomine` in the namespace `cytomine-production`:

    ```bash
    helm install cytomine helm/charts/cytomine -n cytomine-production --create-namespace
    ```

    ::: tip
    `cytomine` can be changed to another release name.

    `cytomine-production` can be changed to another namespace.
    :::

## Upgrade Cytomine

To upgrade Cytomine to the latest version:

1. Fetch the latest changes in the cytomine repository:

   ```bash
   cd cytomine
   git pull
   ```

2. Upgrade to the latest version:

   ```bash
   helm upgrade cytomine helm/charts/cytomine -n cytomine-production
   ```

## Uninstallation

To uninstall Cytomine in Kubernetes:

```bash
helm uninstall cytomine -n cytomine-production
```
