---
title: Installation
redirectFrom:
  - /display/PubOp/Install+Cytomine+on+Linux
  - /How-to-install-Cytomine
  - /admin-guide/ce/ce-install
---

# Install Cytomine

::: tip
If you experiment any issues, please describe your problem precisely [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

## Prerequisites

### Hardware

Cytomine is a set of multithreaded tools. A minimum of **8 CPU cores** is required. The number of required cores is proportional to the expected activity. The more you want to support active users, the more you need cores. For a server-setup configuration, we recommend to use at least **16 cores**.

Regarding memory, a minimum of **8 GB** is required, but we recommend at least **16 GB**.

Cytomine installation requires about **15GB**. You need to provide enough space to store your images (depends on their size). If database backup is enabled, extra-space has to be provided.

### Software

- A **Linux** operating system like [Ubuntu](https://ubuntu.com/), [Debian](https://www.debian.org/), etc.
- [Docker Engine](https://docs.docker.com/get-docker/) (v20.10+ recommended)
- [Docker Compose](https://docs.docker.com/compose/) (v2.0+ recommended)

### Running App in Cytomine

To be able to execute apps in Cytomine, a cluster must be installed and running before proceeding with the installation. See [Clusters](/admin-guide/clusters/).

## Installation

This installation procedure is intended for desktop or laptop computers running Debian-based Linux operating systems.

> It is expected to have `root` permissions (sudo privileges in Debian/Ubuntu).

1. Create a folder for hosting the files:

   ```bash
   mkdir cytomine
   cd cytomine
   ```

2. Download the Docker Compose file on your computer:

   ```bash
   wget https://raw.githubusercontent.com/cytomine/cytomine/main/compose.yaml
   ```

3. Launch cytomine:

   ```bash
   sudo docker compose up -d
   ```

4. Once all services are up and running, Cytomine is ready to be used:

   - If you have kept the default values your Cytomine is now available on <http://127.0.0.1/>.
   - A default admin account is created with the password `password`

::: tip
Once authenticated, it is recommended to update the default administrator password to a more secure one.
:::

## Stop Cytomine

To stop your Cytomine instance:

```bash
sudo docker compose down
```

The server will be stopped, but the data, including databases and images, will be preserved.

## What's Next?

For additional setup options, see the [Configuration](./configuration.md) section.
