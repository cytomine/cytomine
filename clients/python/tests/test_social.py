# -*- coding: utf-8 -*-

from typing import Any

from cytomine.cytomine import Cytomine
from cytomine.models import (
    AnnotationAction,
    AnnotationActionCollection,
    Position,
    PositionCollection,
)

class TestPosition:
    def test_positions(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        positions = PositionCollection().fetch_with_filter(
            "imageinstance",
            dataset["image_instance"].id,
        )
        assert isinstance(positions, PositionCollection)

        if len(positions) > 0:
            position = Position().fetch(positions[0].id)
            assert isinstance(position, Position)

class TestAnnotationAction:
    def test_annotationactions(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        annot_actions = AnnotationActionCollection().fetch_with_filter(
            "imageinstance",
            dataset["image_instance"].id,
        )
        assert isinstance(annot_actions, AnnotationActionCollection)

        if len(annot_actions) > 0:
            annot_action = AnnotationAction().fetch(annot_actions[0].id)
            assert isinstance(annot_action, AnnotationAction)
