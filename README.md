# Cytomine Platform

Cytomine is an open-source platform for collaborative analysis of large-scale imaging data.

This repository provides the necessary files and instructions to build and launch the Cytomine product using Docker Compose.

## Installation

For detailed instructions and additional information, please refer to the [Cytomine installation documentation](https://doc.uliege.cytomine.org/admin-guide/ce/installation)

### Prerequisites

- A **Linux** operating system like [Ubuntu](https://ubuntu.com/), [Debian](https://www.debian.org/), etc.
- [Docker Engine](https://docs.docker.com/get-docker/) (v20.10+ recommended)
- [Docker Compose](https://docs.docker.com/compose/) (v2.0+ recommended)

### Installation

> By default, the docker compose is configured to pull images directly from DockerHub.

Run the following command to start the services:

```bash
docker compose up -d
```

Once all services are running, the application will be accessible at [http://127.0.0.1](http://127.0.0.1).

### Build the Docker Images

To build all required Docker images for Cytomine, run:

```sh
docker compose build
```

### Stop and delete the data from Cytomine

To stop Cytomine and remove its volumes, run:

```sh
docker compose down -v
```

To delete the data and databases, run:

```sh
sudo rm -rf ./data
```

## How to use the Cytomine dev environment

To start a development environment for specific Cytomine services, use the `start-dev` command in the Makefile:

```bash
make dev <service-a> <service-b> ...
```

Where the available services are

| Profile | Description                |
|---------|----------------------------|
| `ae`    | App Engine                 |
| `core`  | Core backend services      |
| `iam`   | Identity Access Management |
| `ims`   | Image Management Server    |
| `ui`    | Web UI frontend            |

> 💡 There must be at least one service in dev mode.

### Examples

Start the App Engine and UI services in development mode:

```bash
make start-dev ae ui
```

> 💡 You can combine multiple profiles as needed.

To stop the development environment, use the `stop-dev` command in the Makefile:

```bash
make stop-dev
```

## License

[Apache 2.0](https://github.com/cytomine/cytomine/blob/main/LICENSE).
