from typing import Dict, Any
from fastapi import HTTPException


def validate_box_feature(feature: Dict[str, Any]) -> None:
    """
    Function to validate a box feature, it ensures that the GeoJSON feature that is received corresponds to a box.

    Args:
        (feature: Dict[str, Any]): Feature.

    Returns:
        (None): Returns nothing but raises HTTPException if needed.
    """
    if feature.get("type") != "Feature":
        raise HTTPException(400, "Geometry must be a GeoJSON Feature.")
    
    geometry = feature.get("geometry", {})
    if geometry.get("type") != "Polygon":
        raise HTTPException(400, "Geometry must be a Polygon.")
    
    coords = geometry.get("coordinates", [])
    if not coords or not coords[0]:
        raise HTTPException(400, "Polygon must have coordinates.")

    box = coords[0]
    if len(box) != 5:
        raise HTTPException(400, "Box must have 5 coordinates (4 corners + closing point).")

    if box[0] != box[-1]:
        raise HTTPException(400, "Box must be closed (first and last coordinates must match).")

    corners = box[:4]
    x_coords = [pt[0] for pt in corners]
    y_coords = [pt[1] for pt in corners]

    unique_x = sorted(set(x_coords))
    unique_y = sorted(set(y_coords))

    if len(unique_x) != 2 or len(unique_y) != 2:
        raise HTTPException(400, "Box must be an axis-aligned rectangle (2 unique x and 2 unique y).")

    normal_corners = [[unique_x[0], unique_y[0]], [unique_x[1], unique_y[0]], [unique_x[1], unique_y[1]], [unique_x[0], unique_y[1]]]

    if sorted(map(tuple, corners)) != sorted(map(tuple, normal_corners)):
        raise HTTPException(400, "Box corners do not form a proper rectangle.")
    

def validate_point_feature(feature: Dict[str, Any]) -> None:
    """
    Function to validate a point feature, it ensures that the GeoJSON feature that is received corresponds to a point.

    Args:
        (feature: Dict[str, Any]): Feature.

    Returns:
        (None): Returns nothing but raises HTTPException if needed.
    """
    if feature.get("type") != "Feature":
        raise HTTPException(400, "Point must be a GeoJSON Feature.")
    
    geometry = feature.get("geometry", {})
    if geometry.get("type") != "Point":
        raise HTTPException(400, "Point geometry must be of type Point.")

    coords = geometry.get("coordinates", [])
    if not isinstance(coords, list) or len(coords) != 2:
        raise HTTPException(400, "Point must have exactly 2 coordinates (x, y).")

    properties = feature.get("properties", {})
    if "label" not in properties or properties["label"] not in (0, 1):
        raise HTTPException(400, "Point must have a 'label' property with value 0 or 1.")
