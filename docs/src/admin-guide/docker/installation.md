---
title: Installation
redirectFrom:
  - /display/PubOp/Install+Cytomine+on+Linux
  - /How-to-install-Cytomine
  - /admin-guide/ce/ce-install
  - /admin-guide/ce/installation
---

# Local Installation — All in Docker Compose

::: tip
If you experience any issues, please describe your problem precisely [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

This guide describes how to run Cytomine locally with **all services deployed as Docker containers** using the root `compose.yaml`. This is the simplest local deployment: no Kubernetes knowledge required, everything is managed by Docker Compose.

A k3s container is included automatically to support App Engine task scheduling; it runs as a sidecar and does not require manual configuration.

## Prerequisites

### Hardware

- Minimum **8 CPU cores** (16+ recommended for multi-user workloads)
- Minimum **8 GB RAM** (16 GB recommended)
- At least **15 GB** of free disk space for images and databases

::: warning
The disk on which Docker is installed **must not exceed 89% utilisation**. Insufficient free space may cause build failures or unstable runtime behaviour.

Check disk usage with:
```bash
df -h
```
:::

### Software

- **Linux** operating system (Ubuntu, Debian, etc.)
- [Docker Engine](https://docs.docker.com/get-docker/) v20.10+
- [Docker Compose](https://docs.docker.com/compose/) v2.0+
- [Git](https://git-scm.com/) v2.0+

## Installation

1. Clone the Cytomine repository:

   ```bash
   git clone https://github.com/cytomine/cytomine.git
   cd cytomine
   ```

2. Start all services:

   ```bash
   docker compose up -d
   ```

   _First startup takes around 10 minutes to pull all Docker images._

3. Cytomine is ready when all containers are healthy. Access the UI at:

   - **Web UI**: <http://127.0.0.1/>
   - **Core API**: <http://127.0.0.1:8080>
   - **PIMS (image server)**: <http://127.0.0.1:5001>
   - **IAM (identity)**: <http://127.0.0.1:8070>
   - **App Engine**: <http://127.0.0.1:8082>

   Default admin credentials: username `admin`, password `password`.

::: tip
If you encounter issues during startup, refer to the [troubleshooting](./troubleshooting.md) guide.
:::

## Services and Ports

| Service       | Port  | Description                              |
|---------------|-------|------------------------------------------|
| `web-ui`      | 80    | Web user interface                       |
| `core`        | 8080  | Main Cytomine API server                 |
| `pims`        | 5001  | Image server (PIMS)                      |
| `iam`         | 8070  | Identity and access management (Keycloak)|
| `app-engine`  | 8082  | Algorithm / task runner                  |
| `cbir`        | 6000  | Content-based image retrieval            |
| `sam`         | 8000  | Segment Anything Model service           |
| `repository`  | 8081  | Artifact repository                      |
| `postgis`     | 5432  | Main PostgreSQL database                 |
| `mongo`       | 27017 | Activity / metadata database             |
| `app-engine-db` | 5433 | App Engine PostgreSQL database          |
| `registry`    | 5000  | Docker image registry for tasks          |
| `k3s`         | 6443  | Kubernetes API (used by App Engine)      |

## Data Persistence

All persistent data (databases, images, registry) is stored under `./data/` by default. Override the location by setting `DATA_PATH` before starting:

```bash
DATA_PATH=/mnt/data docker compose up -d
```

Images to import can be placed in a dataset directory. Override with `IMPORT_PATH`:

```bash
IMPORT_PATH=/path/to/slides docker compose up -d
```

## Building Images Locally

A `compose.override.yaml` file is provided to build Cytomine service images directly from source instead of pulling them from the registry. This is useful for development:

```bash
docker compose up -d  # automatically merges compose.override.yaml if present
```

The override file adds build contexts for: `web-ui`, `pims`, `iam`, `app-engine`, `core`, `cbir`, `sam`, and `repository`.

To pull pre-built images instead (ignoring the override):

```bash
docker compose -f compose.yaml up -d
```

## Upgrade Cytomine

1. Pull the latest repository changes:

   ```bash
   git pull
   ```

2. Pull the latest Docker images:

   ```bash
   docker compose pull
   ```

3. Restart with the new images:

   ```bash
   docker compose up -d
   ```

## Stop Cytomine

```bash
docker compose down
```

Data and databases are preserved. To also remove volumes (destructive — deletes all data):

```bash
docker compose down -v
```

## What's Next?

For additional setup options, see the [Configuration](./configuration.md) section.
