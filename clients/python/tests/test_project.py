from typing import Any, Dict

from cytomine.cytomine import Cytomine
from cytomine.models import Project, ProjectCollection
from tests.conftest import random_string


def test_create_project(dataset: dict[str, Any]) -> None:
    name = random_string()

    project = Project(name, dataset["ontology"].id).save()

    assert isinstance(project, Project)
    assert project.name == name
    assert project.ontology == dataset["ontology"].id


class TestProject:
    def test_project(self, connect: Cytomine, dataset: Dict[str, Any]) -> None:
        name = random_string()
        project = Project(name, dataset["ontology"].id).save()
        assert isinstance(project, Project)
        assert project.name == name

        project = Project().fetch(project.id)
        assert isinstance(project, Project)
        assert project.name == name

        name = random_string()
        project.name = name
        project.update()
        assert isinstance(project, Project)
        assert project.name == name

        project.delete()
        assert not Project().fetch(project.id)

    def test_projects(self, connect: Cytomine, dataset: Dict[str, Any]) -> None:
        projects = ProjectCollection().fetch()
        assert isinstance(projects, ProjectCollection)

    def test_projects_by_user(
        self,
        connect: Cytomine,
        dataset: Dict[str, Any],
    ) -> None:
        projects = ProjectCollection().fetch_with_filter("user", dataset["user"].id)
        assert isinstance(projects, ProjectCollection)

    def test_projects_by_ontology(
        self,
        connect: Cytomine,
        dataset: Dict[str, Any],
    ) -> None:
        projects = ProjectCollection().fetch_with_filter(
            "ontology",
            dataset["ontology"].id,
        )
        assert isinstance(projects, ProjectCollection)
