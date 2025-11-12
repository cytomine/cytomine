from typing import List, Union, cast

import cv2
import geojson
import numpy as np
from shapely import wkt
from shapely.geometry import Polygon, box


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


def has_positive_area(location: str) -> bool:
    """Returns True if geometry has area > 0."""
    geometry = wkt.loads(location)
    return geometry.area > 0


def mask_to_polygon(
    mask: np.ndarray,
    image_height: int,
    offset_x: int,
    offset_y: int,
    scale_x: float = 1.0,
    scale_y: float = 1.0,
) -> Union[geojson.Feature, None]:
    """
    Function to convert the mask to a GeoJSON taking into account the offset due
    to the manipulation of WSIs.

    Args:
        (mask: np.ndarray): the mask.
        (image_height: int): the height of the image.
        (offset_x: int): the offset along the x-axis.
        (offset_y: int): the offset along the y-axis.
        (scale_x: float): the scaling factor to apply to x coordinates.
        (scale_y: float): the scaling factor to apply to y coordinates.

    Returns:
        (geojson.Feature, or None): Returns the structure as a GeoJSON.
    """
    mask = (mask > 0).astype(np.uint8)
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if not contours:
        return None

    polygons: List[List[List[float]]] = []
    for contour in contours:
        if len(contour) >= 3:  # because the polygon must at least have 3 points
            coords = contour.squeeze().astype(np.float32)

            if coords.ndim != 2:
                continue

            coords[:, 0] /= scale_x
            coords[:, 1] /= scale_y

            coords = coords + np.array([offset_x, offset_y])
            coords[:, 1] = image_height - coords[:, 1]

            coords_list = cast(List[List[float]], coords.tolist())

            if coords_list[0] != coords_list[-1]:
                coords_list.append(coords_list[0])

            polygons.append(coords_list)

    if not polygons:
        return None

    if len(polygons) == 1:
        geometry = geojson.Polygon([polygons[0]])
    else:
        geometry = geojson.MultiPolygon([[poly] for poly in polygons])

    feature = geojson.Feature(geometry=geometry, properties={})

    return feature
