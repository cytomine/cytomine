# Cytomine

<div align="center">
  <img alt="Cytomine" src="https://raw.githubusercontent.com/cytomine/cytomine/main/docs/src/.vuepress/public/images/cytomine-uliege-logo.png">
</div>

Cytomine is an open-source platform for collaborative analysis of large-scale imaging data.

This repository provides the necessary files and instructions to build and launch the Cytomine product using Docker Compose.

## Installation

### Local Installation

For installation procedure, please refer to the [local installation documentation](https://doc.uliege.cytomine.org/admin-guide/docker/installation).

### Kubernetes Installation

For installing Cytomine on Kubernetes, please refer to the [Kubernetes installation documentation](https://doc.uliege.cytomine.org/admin-guide/k8s/installation).

### Local dev installation

K3S is provided as a mean to deploy on a local docker-compose environment. Run:
- `make doctor`
- `make init-secrets`
- `make start-k3s-cluster`
- `make deploy-helm`

## License

[Apache 2.0](https://github.com/cytomine/cytomine/blob/main/LICENSE).
