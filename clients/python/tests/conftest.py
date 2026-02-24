import random
import string
from typing import Any, Dict

import pytest
from testcontainers.compose import DockerCompose

from cytomine import Cytomine
from cytomine.models import (
    AbstractImage,
    AbstractSlice,
    Annotation,
    CurrentUser,
    ImageInstance,
    Ontology,
    Project,
    Storage,
    Tag,
    Term,
    Track,
    UploadedFile,
)


def random_string(length: int = 10) -> str:
    return "".join(random.choice(string.ascii_letters) for _ in range(length))


def pytest_addoption(parser: pytest.Parser) -> None:
    parser.addoption("--host", action="store")
    parser.addoption("--public_key", action="store")
    parser.addoption("--private_key", action="store")


@pytest.fixture(scope="session", autouse=True)
def setup_cytomine():
    compose_path = "../../compose.yaml"

    with DockerCompose(".", compose_file_name=compose_path) as compose:
        compose.wait_for("http://127.0.0.1/server/ping")

        stdout, _, _ = compose.exec_in_container(
            [
                "psql",
                "-U",
                "docker",
                "-t",
                "-c",
                "SELECT public_key, private_key FROM sec_user WHERE username = 'admin'",
            ],
            service_name="postgis",
        )
        public_key, private_key = stdout.strip().split("|")

        compose.host = "http://127.0.0.1"
        compose.public_key = public_key.strip()
        compose.private_key = private_key.strip()

        yield compose


@pytest.fixture(scope="session", autouse=True)
def connect(setup_cytomine) -> Cytomine:
    c = Cytomine.connect(
        setup_cytomine.host,
        setup_cytomine.public_key,
        setup_cytomine.private_key,
    )
    c.wait_to_accept_connection()
    c.open_admin_session()
    return c


@pytest.fixture(scope="session")
def dataset(connect: Cytomine, request: pytest.FixtureRequest) -> Dict[str, Any]:
    data: Dict[str, Any] = {}
    data["user"] = CurrentUser().fetch()

    data["ontology"] = Ontology(random_string()).save()
    data["term1"] = Term(random_string(), data["ontology"].id, "#000000").save()
    data["term2"] = Term(random_string(), data["ontology"].id, "#000000").save()

    data["project"] = Project(random_string(), data["ontology"].id).save()
    data["storage"] = Storage(random_string(), data["user"].id).save()
    data["uploaded_file"] = UploadedFile(
        originalFilename=random_string(),
        filename=random_string(),
        size=1,
        ext="tiff",
        contentType="tiff/ddd",
        id_projects=data["project"].id,
        id_storage=data["storage"].id,
        id_user=data["user"].id,
    ).save()
    data["uploaded_file2"] = UploadedFile(
        originalFilename=random_string(),
        filename=random_string(),
        size=1,
        ext="tiff",
        contentType="tiff/ddd",
        id_projects=data["project"].id,
        id_storage=data["storage"].id,
        id_user=data["user"].id,
    ).save()
    data["uploaded_file3"] = UploadedFile(
        originalFilename=random_string(),
        filename=random_string(),
        size=1,
        ext="tiff",
        contentType="tiff/ddd",
        id_projects=data["project"].id,
        id_storage=data["storage"].id,
        id_user=data["user"].id,
    ).save()

    data["abstract_image"] = AbstractImage(
        random_string(),
        data["uploaded_file"].id,
        width=50,
        height=50,
    ).save()
    data["abstract_image2"] = AbstractImage(
        random_string(),
        data["uploaded_file2"].id,
        width=50,
        height=50,
    ).save()
    data["abstract_image3"] = AbstractImage(
        random_string(),
        data["uploaded_file3"].id,
        width=50,
        height=50,
    ).save()

    data["abstract_slice"] = AbstractSlice(
        id_image=data["abstract_image"].id,
        channel=0,
        z_stack=0,
        time=0,
        id_uploaded_file=data["uploaded_file"].id,
        mime="image/pyrtiff",
    ).save()

    data["image_instance"] = ImageInstance(
        data["abstract_image"].id,
        data["project"].id,
    ).save()
    data["image_instance2"] = ImageInstance(
        data["abstract_image2"].id,
        data["project"].id,
    ).save()

    data["track"] = Track(random_string(), data["image_instance2"].id, "#000000").save()

    # data["annotation"] = Annotation(
    #     location="POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))",
    #     id_image=data["image_instance"].id,
    # ).save()

    data["tag"] = Tag(random_string()).save()

    def teardown() -> None:
        ImageInstance().delete(data["image_instance"].id)
        # Annotation().delete(data["annotation"].id)
        AbstractImage().delete(data["abstract_image"].id)
        AbstractImage().delete(data["abstract_image2"].id)
        Term().delete(data["term1"].id)
        Term().delete(data["term2"].id)
        Project().delete(data["project"].id)
        Ontology().delete(data["ontology"].id)
        Tag().delete(data["tag"].id)

    request.addfinalizer(teardown)

    return data
