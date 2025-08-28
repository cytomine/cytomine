"""Module to test the validate.py file"""

import re
import pytest
from fastapi import HTTPException

from src.api.models.validate import validate_box_feature, validate_point_feature


def test_valid_box() -> None:
    """Test function"""
    feature = {
        "type": "Feature",
        "geometry": {
            "type": "Polygon",
            "coordinates": [[[1, 1], [4, 1], [4, 3], [1, 3], [1, 1]]]
        }
    }

    validate_box_feature(feature)

def test_box_invalid_type() -> None:
    """Test function"""
    feature = {"type": "Invalid", "geometry": {}}

    with pytest.raises(HTTPException, match = "Geometry must be a GeoJSON Feature"):
        validate_box_feature(feature)

def test_box_not_polygon() -> None:
    """Test function"""
    feature = {"type": "Feature", "geometry": {"type": "Point"}}

    with pytest.raises(HTTPException, match = "Geometry must be a Polygon"):
        validate_box_feature(feature)

def test_box_missing_coordinates() -> None:
    """Test function"""
    feature = {"type": "Feature", "geometry": {"type": "Polygon", "coordinates": []}}

    with pytest.raises(HTTPException, match = "Polygon must have coordinates"):
        validate_box_feature(feature)

def test_box_not_closed() -> None:
    """Test function"""
    feature = {
        "type": "Feature", 
        "geometry": {
            "type": "Polygon", 
            "coordinates": [[[1, 1], [4, 1], [4, 3], [1, 3]]]
            }
    }

    with pytest.raises(HTTPException, match = "Box must have 5 coordinates"):
        validate_box_feature(feature)

def test_box_open_shape() -> None:
    """Test function"""
    feature = {
        "type": "Feature", 
        "geometry": {
            "type": "Polygon", 
            "coordinates": [[[1, 1], [4, 1], [4, 3], [1, 3], [2, 2]]]
            }
    }

    with pytest.raises(HTTPException, match = "Box must be closed"):
        validate_box_feature(feature)

def test_box_not_axis_aligned() -> None:
    """Test function"""
    feature = {
        "type": "Feature",
        "geometry": {
            "type": "Polygon",
            "coordinates": [[[1, 1], [3, 2], [2, 4], [0, 3], [1, 1]]]
        }
    }

    with pytest.raises(HTTPException, match = "Box must be an axis-aligned rectangle"):
        validate_box_feature(feature)

def test_box_incorrect_rectangle() -> None:
    """Test function"""
    feature = {
        "type": "Feature",
        "geometry": {
            "type": "Polygon",
            "coordinates": [[[1, 1], [4, 1], [4, 2], [1, 4], [1, 1]]]
        }
    }

    error_msg = "400: Box must be an axis-aligned rectangle (2 unique x and 2 unique y)."
    with pytest.raises(HTTPException, match = re.escape(error_msg)):
        validate_box_feature(feature)


def test_valid_point() -> None:
    """Test function"""
    feature = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [1, 2]},
        "properties": {"label": 1}
    }

    validate_point_feature(feature)

def test_point_invalid_type() -> None:
    """Test function"""
    feature = {"type": "Invalid", "geometry": {}}

    with pytest.raises(HTTPException, match = "Point must be a GeoJSON Feature"):
        validate_point_feature(feature)

def test_point_wrong_geometry() -> None:
    """Test function"""
    feature = {"type": "Feature", "geometry": {"type": "Polygon"}}

    with pytest.raises(HTTPException, match = "Point geometry must be of type Point"):
        validate_point_feature(feature)

def test_point_wrong_coords() -> None:
    """Test function"""
    feature = {
        "type": "Feature", 
        "geometry": {"type": "Point", "coordinates": [1]}, 
        "properties": {"label": 0}
    }

    with pytest.raises(HTTPException, match = "Point must have exactly 2 coordinates"):
        validate_point_feature(feature)

def test_point_missing_label() -> None:
    """Test function"""
    feature = {
        "type": "Feature", 
        "geometry": {"type": "Point", "coordinates": [1, 2]}, 
        "properties": {}
    }

    with pytest.raises(HTTPException, match = "Point must have a 'label' property"):
        validate_point_feature(feature)

def test_point_invalid_label() -> None:
    """Test function"""
    feature = {
        "type": "Feature", 
        "geometry": {"type": "Point", "coordinates": [1, 2]}, 
        "properties": {"label": 2}
    }

    with pytest.raises(
        HTTPException,
        match = "Point must have a 'label' property with value 0 or 1"
    ):
        validate_point_feature(feature)
