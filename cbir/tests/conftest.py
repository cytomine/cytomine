"""Pytest configuration and fixtures for the test suite."""

import os
import shutil
import tempfile
from typing import Generator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from redis import Redis  # type: ignore
from testcontainers.redis import RedisContainer

from cbir import app as main
from cbir import config


os.environ["DOCKER_CLIENT_TIMEOUT"] = "300"


@pytest.fixture(scope="session")
def redis_container():
    """Start a Redis container for the test session."""

    image_prefix = os.environ.get("PROXY_CACHE", "")

    with RedisContainer(image=f"{image_prefix}redis:7.2") as container:
        yield container


@pytest.fixture(scope="function", autouse=True)
def redis_client(redis_container: RedisContainer) -> Generator[Redis, None, None]:
    """
    Provide a Redis client for testing and clean up afterward.

    Args:
        redis_container: The Redis container fixture.

    Yields:
        Redis: A Redis client instance.
    """

    client = Redis(
        host=redis_container.get_container_host_ip(),
        port=redis_container.get_exposed_port(6379),
        db=1,
        decode_responses=True,
    )
    yield client
    client.flushdb()
    client.close()


@pytest.fixture(scope="function")
def test_directory() -> Generator[str, None, None]:
    """
    Provide a temporary directory for testing and clean up afterward.

    Yields:
        str: The path to the temporary directory.
    """

    tmp_directory = tempfile.mkdtemp()
    yield tmp_directory
    shutil.rmtree(tmp_directory)


def get_settings(
    test_directory: str,
    redis_container: RedisContainer,
) -> config.Settings:
    """
    Get the tests settings.

    Args:
        test_directory (str): The path to the temporary directory.

    Returns:
        (Settings): The test environment settings.
    """
    return config.Settings(
        data_path=test_directory,
        db=1,
        host=redis_container.get_container_host_ip(),
        port=redis_container.get_exposed_port(6379),
    )


@pytest.fixture
def app(test_directory: str, redis_container: RedisContainer) -> FastAPI:
    """
    Create and provide a FastAPI application instance for testing.

    Args:
        test_directory (str): The path to the temporary directory.

    Returns:
        app: The FastAPI app instance to be tested.
    """

    main.app.dependency_overrides[config.get_settings] = lambda: get_settings(
        test_directory,
        redis_container,
    )

    main.app.state.model = main.load_model(
        get_settings(test_directory, redis_container),
    )

    return main.app


@pytest.fixture
def client(app: FastAPI) -> TestClient:
    """
    Provide a test client for the FastAPI application.

    Args:
        app (FastAPI): The FastAPI application instance to be tested.

    Returns:
        TestClient: An instance of `TestClient`.
    """
    return TestClient(app)
