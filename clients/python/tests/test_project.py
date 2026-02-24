from typing import Any

from cytomine.models import Project, ProjectCollection
from tests.conftest import random_string


def test_create_project(dataset: dict[str, Any]) -> None:
    name = random_string()

    project = Project(name, dataset["ontology"].id).save()

    assert isinstance(project, Project)
    assert project.name == name
    assert project.ontology == dataset["ontology"].id


def test_update_project(dataset: dict[str, Any]) -> None:
    name = random_string()
    project = dataset["project"]

    project.name = name
    project.update()

    assert isinstance(project, Project)
    assert project.name == name
    assert project.ontology == dataset["ontology"].id


def test_delete_project(dataset: dict[str, Any]) -> None:
    name = random_string()
    project = Project(name, dataset["ontology"].id).save()

    project.delete()

    assert not Project().fetch(project.id)


def test_fetch_project(dataset: dict[str, Any]) -> None:
    expected_project = dataset["project"]

    project = Project().fetch(expected_project.id)

    assert isinstance(project, Project)
    assert project.id == expected_project.id
    assert project.ontology == dataset["ontology"].id


def test_fetch_project_collection() -> None:
    projects = ProjectCollection().fetch()

    assert isinstance(projects, ProjectCollection)


def test_fetch_project_collection_by_user(dataset: dict[str, Any]) -> None:
    projects = ProjectCollection().fetch_with_filter("user", dataset["user"].id)

    assert isinstance(projects, ProjectCollection)


def test_fetch_project_collection_by_ontology(dataset: dict[str, Any]) -> None:
    projects = ProjectCollection().fetch_with_filter(
        "ontology",
        dataset["ontology"].id,
    )

    assert isinstance(projects, ProjectCollection)
