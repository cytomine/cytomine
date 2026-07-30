import os
from contextlib import contextmanager

import pytest
from fastapi.testclient import TestClient

from pims import config

os.environ['CONFIG_FILE'] = "./pims-config.env"


def test_root():
    return get_settings().root


def get_settings():
    return config.Settings(
        _env_file=os.getenv("CONFIG_FILE")
    )


@pytest.fixture
def settings():
    return get_settings()


@pytest.fixture
def app():
    from pims import application as main

    main.app.dependency_overrides[config.get_settings] = get_settings
    return main.app


@pytest.fixture
def client(app):
    return TestClient(app)


@pytest.fixture
def image_path_mrxs():
	path = f"{test_root()}/upload_test_mrxs/"
	image = "CMU-2.zip"
	return [path, image]

@pytest.fixture
def image_path_ndpi():
	path = f"{test_root()}/upload_test_ndpi/"
	image = "lombric-c-sagit-111.ndpi"
	return [path, image]

@contextmanager
def not_raises(expected_exc):
    try:
        yield

    except expected_exc:
        raise AssertionError(
            "Did raise exception {0} when it should not!".format(
                repr(expected_exc)
            )
        )

    except Exception as err:
        raise AssertionError(
            "An unexpected exception {0} raised.".format(repr(err))
        )
