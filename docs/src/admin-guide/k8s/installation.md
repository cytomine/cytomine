# Install Cytomine on Kubernetes

::: tip
If you experiment any issues, please describe your problem precisely [in our ticket system on Github.](https://github.com/cytomine/cytomine-helm/issues)
:::

## Prerequisites

- Git
- A Kubernetes cluster
- [Helm](https://helm.sh/) (v3.0+ recommended)

## Installation

1. Clone the Helm chart repository:

    ```bash
    git clone https://github.com/cytomine/cytomine-helm.git
    cd cytomine-helm
    ```

2. Configure the values in `cytomine-helm/charts/cytomine/values.yaml`:

    ```yaml
    # Your domain name
    global:
      domainName: null
    ```

    All other values can use the default settings.

3. Install the Cytomine Helm chart with a release name `cytomine` in the namespace `cytomine-production`:

    ```bash
    helm install cytomine charts/cytomine -n cytomine-production --create-namespace
    ```

    ::: tip
    `cytomine` can be changed to another release name.

    `cytomine-production` can be changed to another namespace.
    :::

## Uninstallation

To uninstall Cytomine in Kubernetes:

```bash
helm uninstall cytomine -n cytomine-production
```
