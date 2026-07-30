# -*- coding: utf-8 -*-

import json
from typing import Any

from cytomine.cytomine import Cytomine

class Model:
    def __init__(self, **attributes: Any) -> None:
        # In some cases, a model can have some request parameters.
        self._query_parameters: dict[str, Any] = {}

        # Attributes common to all models
        self.id: int | None = None
        self.created = None
        self.updated = None
        self.deleted = None
        self.name: str | None = None

    def fetch(self, id: int | None = None) -> "bool | Model":
        if self.id is None and id is None:
            raise ValueError("Cannot fetch a model with no ID.")
        if id is not None:
            self.id = id

        return Cytomine.get_instance().get_model(self, self.query_parameters)

    def save(self) -> "bool | Model":
        if self.id is None:
            return Cytomine.get_instance().post_model(self)

        return self.update()

    def delete(self, id: int | None = None) -> bool:
        if self.id is None and id is None:
            raise ValueError("Cannot delete a model with no ID.")
        if id is not None:
            self.id = id

        return Cytomine.get_instance().delete_model(self)

    def update(
        self,
        id: int | None = None,
        **attributes: Any,
    ) -> "bool | Model":
        if self.id is None and id is None:
            raise ValueError("Cannot update a model with no ID.")
        if id is not None:
            self.id = id

        if attributes:
            self.populate(attributes)
        return Cytomine.get_instance().put_model(self)

    def is_new(self) -> bool:
        return self.id is None

    def populate(self, attributes: dict[Any, Any]) -> "Model":
        if attributes:
            for key, value in attributes.items():
                if key.startswith("id_"):
                    key = key[3:]
                if key == "uri":
                    key = "uri_"
                if not key.startswith("_"):
                    if key == "class":
                        key += "_"
                    setattr(self, key, value)
        return self

    def to_json(self, **dump_parameters: Any) -> str:
        d = dict(
            (k, v)
            for k, v in self.__dict__.items()
            if v is not None and not k.startswith("_")
        )
        if "uri_" in d:
            d["uri"] = d.pop("uri_")
        return json.dumps(d, **dump_parameters)

    def uri(self) -> str:
        if self.is_new():
            return f"{self.callback_identifier}.json"

        return f"{self.callback_identifier}/{self.id}.json"

    @property
    def query_parameters(self) -> dict[str, Any]:
        return self._query_parameters

    @property
    def callback_identifier(self) -> str:
        return self.__class__.__name__.lower()

    def __str__(self) -> str:
        return f"[{self.callback_identifier}] {self.id} : {self.name}"

class DomainModel(Model):
    def __init__(self, object: "Model", **attributes: Any) -> None:
        super().__init__(**attributes)

        if object.is_new():
            raise ValueError("The object must be fetched or saved before.")

        self.domainClassName: str | None = None
        self.domainIdent: int | None = None
        self.obj = object

    def uri(self) -> str:
        if self.is_new():
            return (
                f"domain/{self.domainClassName}/{self.domainIdent}/"
                f"{self.callback_identifier}.json"
            )

        return (
            f"domain/{self.domainClassName}/{self.domainIdent}/"
            f"{self.callback_identifier}/{self.id}.json"
        )

    @property
    def obj(self) -> "Model":
        return self._object

    @obj.setter
    def obj(self, value: "Model") -> None:
        self._object = value
        self.domainClassName = getattr(value, "class_", None)
        self.domainIdent = value.id
