import os
import shutil
from contextlib import contextmanager
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

from pims import config

CLEAR_AT_SHUTDOWN = False
TEST_DATA_PATH = Path(os.path.dirname(__file__)) / "test_data"


with open(os.path.join(os.path.dirname(__file__), "fake_files.csv"), "r") as f:
    lines = f.read().splitlines()
    _fake_files = dict()
    for line in lines[1:]:
        filetype, filepath, link, role, kind = line.split(",")
        _fake_files[filepath] = {
            "filetype": filetype,
            "filepath": filepath,
            "link": link,
            "role": role,
            "collection": (kind == "collection"),
        }

fake_files_info = _fake_files.values()


def create_fake_files(fake_files):
    root = Path(test_root())
    for ff in fake_files.values():
        path = root / Path(ff["filepath"])
        path.parent.mkdir(exist_ok=True, parents=True)

        if ff["filetype"] == "f":
            path.touch(exist_ok=True)
        elif ff["filetype"] == "d":
            path.mkdir(exist_ok=True, parents=True)
        elif ff["filetype"] == "l" and not path.exists():
            link = root / Path(ff["link"])
            target_is_directory = (
                True if fake_files[ff["link"]]["filetype"] == "d" else False
            )
            path.symlink_to(link, target_is_directory=target_is_directory)


@pytest.fixture(scope="session")
def fake_files(request):
    create_fake_files(_fake_files)

    def teardown():
        if CLEAR_AT_SHUTDOWN:
            shutil.rmtree(test_root())

    request.addfinalizer(teardown)
    return _fake_files


@pytest.fixture(scope="session", autouse=True)
def load_test_data():
    root = Path(test_root())
    for item in TEST_DATA_PATH.iterdir():
        dest_path = root / item.name
        if item.is_dir():
            shutil.copytree(item, dest_path, dirs_exist_ok=True)
        else:
            shutil.copy2(item, dest_path)


def test_root():
    return get_settings().root


def get_settings():
    return config.Settings(_env_file=os.getenv("CONFIG_FILE"))


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
def root():
    return test_root()


@pytest.fixture
def image_path_jpeg():
    filename = "test-image.jpeg"
    path = f"{test_root()}/upload_test_jpeg/"
    return path, filename


@pytest.fixture
def image_path_png():
    filename = "test-image.png"
    path = f"{test_root()}/upload_test_png/"
    return path, filename


@pytest.fixture
def image_path_tiff():
    filename = "earthworm-transv-posterior-to-clitellum-02.tiff"
    path = f"{test_root()}/upload_test_tiff/"
    return path, filename


@pytest.fixture
def image_path_excentric_filename():
    filename = "Test special char %(_!.tiff"
    path = f"{test_root()}/upload_test_excentric"
    return path, filename


@contextmanager
def not_raises(expected_exc):
    try:
        yield

    except expected_exc:
        raise AssertionError(
            f"Did raise exception {repr(expected_exc)} when it should not!"
        )

    except Exception as err:
        raise AssertionError(f"An unexpected exception {repr(err)} raised.")
