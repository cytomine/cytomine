import docker


def main():
    client = docker.from_env()

    container_config = {
        "image": "registry.cytomine.org/docker/redis:7.2",
        "ports": {"6379/tcp": 6379},
        "tty": False,
        "stdin_open": False,
        "detach": True,
        "labels": {
            "org.testcontainers": "true",
            "org.testcontainers.version": "4.13.3",
            "org.testcontainers.session-id": "af6bd401-116d-4fbb-b756-aeefc180d94a",
        },
    }

    container = client.containers.create(**container_config)
    print(f"Created container with ID: {container.id}")

    container.start()
    print(f"Container {container.id} started!")

    container.stop()
    print(f"Container {container.id} stopped!")


if __name__ == "__main__":
    main()
