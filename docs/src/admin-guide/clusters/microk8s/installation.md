---
title: Installation guide
---

# {{ $frontmatter.title }}

This section describes the installation procedure for MicroK8s.

## Prerequisites

Before installing MicroK8s, the following requirements are needed:

- A **Linux** operating system like [Ubuntu](https://ubuntu.com/), [Debian](https://www.debian.org/), etc.
- [Snap package manager](https://snapcraft.io/docs/installing-snapd) installed.
- A user account with sudo privileges.

## Installation

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

## What's Next?

Configure MicroK8s to ensure proper operations with Cytomine. Detailed instructions are provided in the [Configuration](./configuration.md) section.
