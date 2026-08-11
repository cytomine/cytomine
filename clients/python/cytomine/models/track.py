# -*- coding: utf-8 -*-

from typing import Any

from cytomine.cytomine import Cytomine
from cytomine.models.collection import Collection
from cytomine.models.model import Model

class Track(Model):
    def __init__(
        self,
        name: str | None = None,
        id_image: int | None = None,
        color: str | None = None,
        **attributes: Any,
    ) -> None:
        super().__init__()
        self.name = name
        self.image = id_image
        self.color = color
        self.populate(attributes)

class TrackCollection(Collection):
    def __init__(
        self,
        filters: dict[str, Any] | None = None,
        max: int = 0,
        offset: int = 0,
        **parameters: Any,
    ) -> None:
        super().__init__(Track, filters, max, offset)
        self._allowed_filters = ["project", "imageinstance"]
        self.set_parameters(parameters)

class AnnotationTrack(Model):
    def __init__(
        self,
        annotation_class_name: str | None = None,
        id_annotation: int | None = None,
        id_track: int | None = None,
        **attributes: Any,
    ) -> None:
        super().__init__()
        self.annotationClassName = annotation_class_name
        self.annotationIdent = id_annotation
        self.track = id_track
        self.populate(attributes)

    def uri(self) -> str:
        return f"annotationtrack/{self.annotationIdent}/{self.track}.json"

    def fetch(
        self,
        id_annotation: int | None = None,
        id_track: int | None = None,
    ) -> bool | Model:
        self.id = -1

        if self.annotationIdent is None and id_annotation is None:
            raise ValueError("Cannot fetch a model with no annotation ID.")

        if self.track is None and id_track is None:
            raise ValueError("Cannot fetch a model with no term ID.")

        if id_annotation is not None:
            self.annotationIdent = id_annotation

        if id_track is not None:
            self.track = id_track

        return Cytomine.get_instance().get_model(self, self.query_parameters)

    def update(self, *args: Any, **kwargs: Any) -> bool | Model:
        raise NotImplementedError("Cannot update a annotation-track.")

    def __str__(self) -> str:
        return (
            f"[{self.callback_identifier}] Annotation {self.annotationIdent} "
            f"- Track {self.track}"
        )
