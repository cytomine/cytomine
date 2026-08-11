# -*- coding: utf-8 -*-

from typing import Any

from cytomine import Cytomine
from cytomine.models.collection import Collection
from cytomine.models.model import Model

class Project(Model):
    def __init__(
        self,
        name: str | None = None,
        id_ontology: int | None = None,
        **attributes: Any,
    ) -> None:
        super().__init__()
        self.name = name
        self.ontology = id_ontology
        self.ontologyName = None
        self.blindMode = None
        self.numberOfSlides = None
        self.numberOfImages = None
        self.numberOfAnnotations = None
        self.retrievalProjects = None
        self.numberOfReviewedAnnotations = None
        self.retrievalDisable = None
        self.retrievalAllOntology = None
        self.isClosed = None
        self.isReadOnly = None
        self.hideUsersLayers = None
        self.hideAdminsLayers = None

        self.admins = None
        self.users = None
        self.mode = None
        self.populate(attributes)

    def add_user(
        self,
        id_user: int,
        admin: bool = False,
    ) -> bool | dict[str, Any]:
        if admin:
            return Cytomine.get_instance().post(
                f"project/{self.id}/user/{id_user}/admin.json"
            )

        return Cytomine.get_instance().post(f"project/{self.id}/user/{id_user}.json")

    def delete_user(self, id_user: int, admin: bool = False) -> bool:
        if admin:
            return Cytomine.get_instance().delete(
                f"project/{self.id}/user/{id_user}/admin.json"
            )

        return Cytomine.get_instance().delete(f"project/{self.id}/user/{id_user}.json")

class ProjectCollection(Collection):
    def __init__(
        self,
        filters: dict[str, Any] | None = None,
        max: int = 0,
        offset: int = 0,
        **parameters: Any,
    ) -> None:
        super().__init__(Project, filters, max, offset)
        self._allowed_filters = [None, "user", "ontology"]
        self.set_parameters(parameters)

    def save(self, *args: Any, **kwargs: Any) -> bool | Collection:
        raise NotImplementedError("Cannot save a project collection by client.")
