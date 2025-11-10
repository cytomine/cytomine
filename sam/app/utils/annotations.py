"""Module to handle the smart fetching of the point annotations."""

from typing import Any, Dict, List

import geojson
from cytomine import Cytomine
from cytomine.models import Annotation
from shapely import wkt
from shapely.geometry import LinearRing, LineString, Point, Polygon, box, shape

from app.config import Settings


def filter_point_annotations_within_polygon(
    box_: Polygon,
    annotations: List[Dict[str, Any]],
) -> List[Dict[str, Any]]:
    """
    Function to filter an array of annotations to only keep the point annotations that
    are inside of the box polygon.

    Args:
        (box: Polygon): the box in which the points are kept.
        (annotations: List[Dict[str, Any]]): the annotations to filter.

    Returns:
        (List[Dict[str, Any]]): Returns the point inside the box.
    """
    return [
        ann
        for ann in annotations
        if isinstance(ann["geometry"], Point) and box_.contains(ann["geometry"])
    ]


def get_annotation_by_id(annotation_id: int, settings: Settings) -> Annotation:
    """
    Function to get an annotation by its id.

    Args:
        (annotation_id: int): the id of the annotation to fetch.
        (settings: Settings): the settings.

    Returns:
        (Annotation): Returns the annotation.
    """
    with Cytomine(
        settings.CYTOMINE_HOST,
        settings.CYTOMINE_PUBLIC_KEY,
        settings.CYTOMINE_PRIVATE_KEY,
        verbose=False,
    ):
        annotation = Annotation()
        annotation.id = annotation_id

        annotation.fetch()

    return annotation


def get_bbox_from_annotation(location: str) -> Polygon:
    """
    Function to get a bounding box around the annotation.

    Args:
        (location: str): the geometry of the annotation, as a string.

    Returns:
        (Polygon): Returns the bounding box.
    """
    geometry = wkt.loads(location)
    bbox = box(*geometry.bounds)

    return bbox


def update_annotation_location(
    annotation_id: int,
    new_location: geojson.Feature,
    settings: Settings,
) -> bool:
    """
    Function to update the location of an annotation.

    Args:
        (annotation_id: int): the id of the annotation to fetch.
        (new_location: geojson.Feature): the new location.
        (settings: Settings): the settings.

    Returns:
        (bool): Returns the status of the update (True = ok).
    """
    shapely_geometry = shape(new_location.geometry)
    new_location_wkt = shapely_geometry.wkt

    with Cytomine(
        settings.CYTOMINE_HOST,
        settings.CYTOMINE_PUBLIC_KEY,
        settings.CYTOMINE_PRIVATE_KEY,
        verbose=False,
    ):
        annotation = Annotation()
        annotation.id = annotation_id

        annotation.fetch()

        annotation.location = new_location_wkt
        update_status = annotation.update()

        if update_status is False:
            return False

        return True


def is_invalid_annotation(ann: Annotation) -> bool:
    """
    Function to tell if an annotation is invalid to process or
    not.

    Points are invalid to process because they do not have a bounding
    box, any other annotation with no area or no perimeter is also
    invalid.

    Args:
        (ann: Annotation): the annotation to process.

    Returns:
        (bool): Returns a boolean telling if the annotation is invalid.
    """
    geom = wkt.loads(ann.location)

    if (
        isinstance(geom, Point)
        or isinstance(geom, LineString)
        or isinstance(geom, LinearRing)
        or ann.area == 0.0
        or ann.perimeter == 0.0
    ):
        return True

    return False
