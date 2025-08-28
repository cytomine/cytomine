"""Module to handle the smart fetching of the point annotations."""

from typing import List, Dict, Any

import geojson

from shapely import wkt
from shapely.geometry import Point, Polygon, LineString, LinearRing, box, shape

from cytomine import Cytomine
from cytomine.models import AnnotationCollection, Annotation

from src.config import Settings


def filter_point_annotations_within_polygon(
        box_: Polygon,
        annotations: List[Dict[str, Any]]
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
        ann for ann in annotations
        if isinstance(ann["geometry"], Point) and box_.contains(ann["geometry"])
    ]


def fetch_included_annotations(
        image_id: int,
        user_id: int,
        box_: Polygon,
        settings: Settings,
        delete_annotations: bool = True
    ) -> List[Dict[str, Any]]:
    """
    Function to fetch the user annotations that are included in the geometry.
    The fetched annotations are only point annotations that are included in the
    box geometry. This function can optionally delete those point annotations if
    they are not useful anymore.

    Args:
        (image_id: int): the id of the image to fetch the annotations from.
        (user_id: int): the id of the user whom annotations are fetched.
        (box_: Polygon): the box geometry for this image.
        (settings: Settings): the settings.
        (delete_annotations: bool): whether to delete the point annotations afterwards.

    Returns:
        (List[Dict[str, Any]]): Returns the point prompts formatted as GeoJSON.
    """
    with Cytomine(settings.keys['host'], settings.keys['public_key'],
                  settings.keys['private_key'], verbose = False):

        annotations = AnnotationCollection()
        annotations.image = image_id
        annotations.user = user_id
        annotations.showWKT = True
        annotations.showMeta = True
        annotations.showGIS = True

        annotations.fetch()

        annotation_list = []
        for annotation in annotations:
            annotation_geometry = wkt.loads(annotation.location)
            annotation_list.append({
                "id": annotation.id,
                "geometry": annotation_geometry
            })

        filtered_annotation_list = filter_point_annotations_within_polygon(box_, annotation_list)
        annotation_id_list = [ann["id"] for ann in filtered_annotation_list]

        if delete_annotations:
            for ann in annotations:
                if ann.id in annotation_id_list:
                    ann.delete()

    return annotations_to_geojson_features(filtered_annotation_list)


def annotations_to_geojson_features(annotations: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Function to convert the annotations to the GeoJSON format.

    Args:
        (annotations: List[Dict[str, Any]]): the annotations to convert.

    Returns:
        (List[Dict[str, Any]]): Returns the annotations in GeoJSON format.
    """
    features = []

    for ann in annotations:
        geom = ann["geometry"]

        if isinstance(geom, Point):
            features.append({
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [geom.x, geom.y]
                },
                "properties": {
                    "label": 1
                }
            })

    return features


def get_annotation_by_id(annotation_id: int, settings: Settings) -> Annotation:
    """
    Function to get an annotation by its id.

    Args:
        (annotation_id: int): the id of the annotation to fetch.
        (settings: Settings): the settings.

    Returns:
        (Annotation): Returns the annotation.
    """
    with Cytomine(settings.keys['host'], settings.keys['public_key'],
                  settings.keys['private_key'], verbose = False):

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
        settings: Settings
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

    with Cytomine(settings.keys['host'], settings.keys['public_key'],
                  settings.keys['private_key'], verbose = False):

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

    if isinstance(geom, Point) or isinstance(geom, LineString) or isinstance(geom, LinearRing) or ann.area == 0.0 or ann.perimeter == 0.0:
        return True

    return False
