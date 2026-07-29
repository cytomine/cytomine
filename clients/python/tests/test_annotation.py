# -*- coding: utf-8 -*-

from typing import Any

from cytomine.cytomine import Cytomine
from cytomine.models import Annotation, AnnotationCollection, AnnotationTerm

class TestAnnotation:
    def test_annotation(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        location = "POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))"
        annotation = Annotation(
            location,
            dataset["image_instance"].id,
            [dataset["term1"].id],
        ).save()
        assert isinstance(annotation, Annotation)
        assert annotation.location == location

        annotation = Annotation().fetch(annotation.id)
        assert isinstance(annotation, Annotation)
        assert annotation.location == location

        location = "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))"
        annotation.location = location
        annotation.update()
        assert isinstance(annotation, Annotation)
        assert annotation.location == location

        annotation.delete()
        assert not Annotation().fetch(annotation.id)

    def test_annotation_dump(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        pass

    def test_annotations(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        annotations = AnnotationCollection()
        annotations.showMeta = True
        annotations.showWKT = True
        annotations.fetch()
        assert isinstance(annotations, AnnotationCollection)

        location = "POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))"
        annotations = AnnotationCollection()
        annotations.append(
            Annotation(
                location,
                dataset["image_instance"].id,
                [dataset["term1"].id],
            )
        )
        assert annotations.save()

    def test_annotations_by_project(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        annotations = AnnotationCollection()
        annotations.project = dataset["project"].id
        annotations.fetch()
        assert isinstance(annotations, AnnotationCollection)

class TestAnnotationTerm:
    def test_annotation_term(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        annotation_term = AnnotationTerm(dataset["annotation"].id, dataset["term2"].id)
        assert isinstance(annotation_term, AnnotationTerm)
        assert annotation_term.term == dataset["term2"].id

        annotation_term = AnnotationTerm().fetch(  # type: ignore
            dataset["annotation"].id,
            dataset["term2"].id,
        )
        assert isinstance(annotation_term, AnnotationTerm)
        assert annotation_term.term == dataset["term2"].id

        annotation_term.delete()
        assert not AnnotationTerm().fetch(dataset["annotation"].id, dataset["term2"].id)
