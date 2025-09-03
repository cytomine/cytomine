---
title: Installation guide
---

# {{ $frontmatter.title }}

This section describes the installation procedure for MicroK8s.

## Prerequisites

Before installing MicroK8s, the following requirements are needed:

- A Linux machine
- A user account with sudo privileges
- Snap package manager installed
  - To install it, refer to the [Snap installation guide](https://snapcraft.io/docs/installing-snapd).

## Installation

Using Snap:

```bash
sudo snap install microk8s --classic
```

To verify if the installation was completed:

```bash
microk8s.status --wait-ready
```

The output should be similar to:

```bash
microk8s is running
high-availability: no
  datastore master nodes: 127.0.0.1:19001
  datastore standby nodes: none
addons:
  enabled:
    dashboard            # (core) The Kubernetes dashboard
    dns                  # (core) CoreDNS
    ha-cluster           # (core) Configure high availability on the current node
    helm                 # (core) Helm - the package manager for Kubernetes
    helm3                # (core) Helm 3 - the package manager for Kubernetes
    metrics-server       # (core) K8s Metrics Server for API access to service metrics
  disabled:
    cert-manager         # (core) Cloud native certificate management
    cis-hardening        # (core) Apply CIS K8s hardening
    community            # (core) The community addons repository
    gpu                  # (core) Alias to nvidia add-on
    host-access          # (core) Allow Pods connecting to Host services smoothly
    hostpath-storage     # (core) Storage class; allocates storage from host directory
    ingress              # (core) Ingress controller for external access
    kube-ovn             # (core) An advanced network fabric for Kubernetes
    mayastor             # (core) OpenEBS MayaStor
    metallb              # (core) Loadbalancer for your Kubernetes cluster
    minio                # (core) MinIO object storage
    nvidia               # (core) NVIDIA hardware (GPU and network) support
    observability        # (core) A lightweight observability stack for logs, traces and metrics
    prometheus           # (core) Prometheus operator for monitoring and logging
    rbac                 # (core) Role-Based Access Control for authorisation
    registry             # (core) Private image registry exposed on localhost:32000
    rook-ceph            # (core) Distributed Ceph storage using Rook
    storage              # (core) Alias to hostpath-storage add-on, deprecated
```

The following add-ons must be enabled:

- `dns`
- `ha-cluster`
- `helm`
- `helm3`

If any of the required add-ons are not enabled, you can enable them using the following command:

```bash
microk8s.enable <add-on-name>
```

### GPU Support

If you have a GPU, note that only NVIDIA GPUs are currently supported by MicroK8s:

```bash
microk8s.enable nvidia
```

```bash
Infer repository core for addon nvidia
Addon core/dns is already enabled
Addon core/helm3 is already enabled
Checking if NVIDIA driver is already installed
GPU 0: NVIDIA GeForce GTX 1650 Ti with Max-Q Design (UUID: GPU-ac64f81c-c1bf-4e5e-b930-83b9f092e33a)
"nvidia" already exists with the same configuration, skipping
Hang tight while we grab the latest from your chart repositories...
...Successfully got an update from the "nvidia" chart repository
Update Complete. ⎈Happy Helming!⎈
Deploy NVIDIA GPU operator
Using host GPU driver
NAME: gpu-operator
LAST DEPLOYED: Fri Mar 14 11:14:26 2025
NAMESPACE: gpu-operator-resources
STATUS: deployed
REVISION: 1
TEST SUITE: None
Deployed NVIDIA GPU operator
```

If you encounter issues with the GPU addon, refer to the [official documentation](https://microk8s.io/docs/addon-gpu) for further guidance.

## Add registry

Before installing the Cytomine, you will have to set up a configuration file to allow MicroK8s to communicate with the private registry.

1. Get the IP address of the registry container, which is by default `172.18.0.10` in the [docker-compose.yml](https://github.com/cytomine/cytomine/blob/main/cytomine-community-edition/docker-compose.yml#L106) from the community edition folder.

2. Create the directory and configuration file:

   - `mkdir -p /var/snap/microk8s/current/args/certs.d/172.18.0.10:5000`
   - `touch /var/snap/microk8s/current/args/certs.d/172.18.0.10:5000/hosts.toml`

   > Normally, sudo privileges are not required.

3. Add the following lines inside the `hosts.toml` file:

   ```toml
   server = "http://172.18.0.10:5000"

   [host."http://172.18.0.10:5000"]
   capabilities = ["pull", "resolve"]
   ```

4. Restart MicroK8s to have the new configuration loaded:

   ```bash
   microk8s stop
   ```

   ```bash
   microk8s start
   ```

> More information are available at the [_How to work with a private registry_](https://microk8s.io/docs/registry-private) section of the official documentation of MicroK8s.

MicroK8s is now ready for use by Cytomine.

## Cytomine Configuration

You can now proceed with the installation of [Cytomine](/admin-guide/ce/installation#installation).

In Cytomine, you will have to provide two configurations for the App Engine in the `cytomine.template` file, namely `SCHEDULER_MASTER_URL` and `SCHEDULER_OAUTH_TOKEN`, which can be obtained by the following commands:

1. For the MicroK8s `SCHEDULER_MASTER_URL`: `microk8s.config | grep server`
2. For the `SCHEDULER_OAUTH_TOKEN`: `microk8s kubectl create token default`

::: warning
:warning: Be aware that each time the MicroK8s cluster is restarted, the previous tokens are not valid anymore!
:::
