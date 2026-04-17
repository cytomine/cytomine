---
title: Local k3s Installation
---

# Local Installation — k3s + Helm

::: tip
If you experience any issues, please describe your problem precisely [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

::: warning
This deployment mode is under active development and may be unstable. Use it at your own risk.
:::

This guide describes how to run Cytomine locally with **k3s running as a Docker container** and **all Cytomine services deployed inside Kubernetes via Helm**. This mirrors a production Kubernetes deployment while remaining self-contained on a developer workstation.

This deployment uses:
- `helm/compose.yaml` — starts the k3s container and exposes its Kubernetes API
- `helm/charts/cytomine/` — the Helm chart that deploys all Cytomine services into k3s

## Prerequisites

### Hardware

- Minimum **8 CPU cores** (16+ recommended)
- Minimum **8 GB RAM** (16 GB recommended)
- At least **15 GB** of free disk space

### Software

- **Linux** operating system (Ubuntu, Debian, etc.)
- [Docker Engine](https://docs.docker.com/get-docker/) v20.10+
- [Docker Compose](https://docs.docker.com/compose/) v2.0+
- [Git](https://git-scm.com/) v2.0+
- [Helm](https://helm.sh/docs/intro/install/) v3.0+
- `kubectl` (optional, for inspecting the cluster)

## Host Setup

Cytomine will be served at `cytomine.local`. Add the k3s container's fixed IP to your `/etc/hosts`:

```bash
echo "172.16.238.15 cytomine.local" | sudo tee -a /etc/hosts
```

## Installation

1. Clone the Cytomine repository:

   ```bash
   git clone https://github.com/cytomine/cytomine.git
   cd cytomine
   ```

2. Start the k3s container:

   ```bash
   docker compose -f ./helm/compose.yaml up -d
   ```

   k3s will start and write its kubeconfig to `.kube/shared/config`. Wait until the file appears and the cluster is ready:

   ```bash
   # Poll until the kubeconfig is available (usually under a minute)
   until kubectl --kubeconfig=./.kube/shared/config get nodes 2>/dev/null; do
     echo "Waiting for k3s to be ready..."; sleep 5
   done
   ```

   The k3s container:
   - Forwards Docker's internal DNS so that pods can resolve Docker service names
   - Fixes cgroup v2 compatibility issues automatically via its entrypoint
   - Disables Traefik and uses nginx-ingress instead
   - Pre-creates the `cytomine-local` and `cytomine-local-engine-tasks` namespaces

3. Deploy Cytomine with Helm:

   ```bash
   helm upgrade --kubeconfig=./.kube/shared/config \
     -f ./helm/charts/cytomine/values.yaml \
     cytomine-platform ./helm/charts/cytomine/ \
     -n cytomine-local --install
   ```

   _First deployment takes around 10 minutes while Kubernetes pulls all images._

4. Once all pods are running, Cytomine is available at:

   - **Web UI**: <http://cytomine.local/>

   Default admin credentials: username `admin`, password `password`.

::: tip
Monitor pod startup with:
```bash
kubectl --kubeconfig=./.kube/shared/config get pods -n cytomine-local -w
```
:::

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  Host machine                                        │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │  Docker network (172.16.238.0/24)             │   │
│  │                                               │   │
│  │  ┌────────────────────────────────────────┐  │   │
│  │  │  k3s container (172.16.238.15)          │  │   │
│  │  │                                         │  │   │
│  │  │  Kubernetes pods (cytomine-local ns):   │  │   │
│  │  │    core · pims · web-ui · iam           │  │   │
│  │  │    app-engine · cbir · sam              │  │   │
│  │  │    postgres · mongodb · redis           │  │   │
│  │  │    registry · repository                │  │   │
│  │  │                                         │  │   │
│  │  │  nginx-ingress → cytomine.local:80      │  │   │
│  │  └────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

## kubeconfig

The cluster's kubeconfig is written to `.kube/shared/config` by k3s. Pass it explicitly to `helm` and `kubectl`:

```bash
export KUBECONFIG=$(pwd)/.kube/shared/config
```

or use the `--kubeconfig` flag on each command.

## Private Registry Access (optional)

If your Cytomine images are hosted in a private registry, create pull secrets before deploying:

```bash
kubectl --kubeconfig=./.kube/shared/config \
  create secret docker-registry my-pull-secret \
  --docker-server=<registry> \
  --docker-username=<user> \
  --docker-password=<token> \
  -n cytomine-local
```

Then add the secret name to your Helm values:

```yaml
images:
  imagePullSecretsNames:
    - my-pull-secret
```

Alternatively, use the provided override compose file that mounts pull secrets directly into k3s:

```bash
docker compose -f ./helm/compose.yaml \
  -f ./helm/docker/compose.docker-pull.yaml \
  up -d
```

## Upgrade Cytomine

1. Pull the latest repository changes:

   ```bash
   git pull
   ```

2. Re-run the Helm upgrade:

   ```bash
   helm upgrade --kubeconfig=./.kube/shared/config \
     -f ./helm/charts/cytomine/values.yaml \
     cytomine-platform ./helm/charts/cytomine/ \
     -n cytomine-local
   ```

## Stop Cytomine

Remove the Helm release (stops all pods but preserves PVCs):

```bash
helm uninstall --kubeconfig=./.kube/shared/config \
  cytomine-platform -n cytomine-local
```

Stop and remove the k3s container:

```bash
docker compose -f ./helm/compose.yaml down
```

To also remove all persistent volumes (destructive — deletes all data):

```bash
docker compose -f ./helm/compose.yaml down -v
```

## Differences from the All-in-Docker-Compose Deployment

| Aspect | All-in-Docker-Compose | k3s + Helm |
|--------|-----------------------|------------|
| Access URL | `http://127.0.0.1/` | `http://cytomine.local/` |
| Orchestration | Docker Compose | Kubernetes (k3s) + Helm |
| Configuration | Environment variables in `compose.yaml` | Helm `values.yaml` |
| Helm required | No | Yes |
| `/etc/hosts` entry required | No | Yes |
| Mirrors production k8s setup | No | Yes |
| k3s used for | App Engine tasks only | All Cytomine services |
