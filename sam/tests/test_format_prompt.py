"""Module to test the format_prompt.py file"""

import pytest
import numpy as np

from src.utils.format_prompt import format_box_prompt, format_point_prompt


def test_format_point_prompt_valid() -> None:
    """Test function"""
    points = [
        {"geometry": {"coordinates": [5, 10]}, "properties": {"label": 1}},
        {"geometry": {"coordinates": [20, 30]}, "properties": {"label": 0}}
    ]

    coords, labels = format_point_prompt(points)

    expected_coords = np.array([[5, 10], [20, 30]], dtype = np.int32)
    expected_labels = np.array([1, 0], dtype = np.int32)

    np.testing.assert_array_equal(coords, expected_coords)
    np.testing.assert_array_equal(labels, expected_labels)


def test_format_point_prompt_empty() -> None:
    """Test function"""
    coords, labels = format_point_prompt([])

    assert coords is None
    assert labels is None


def test_format_point_prompt_invalid() -> None:
    """Test function"""
    with pytest.raises(ValueError, match = "Invalid point data format"):
        format_point_prompt([
            {"geometry": {"coordinates": ["a", "b"]}, "properties": {"label": "x"}}
        ])


def test_format_box_prompt_valid() -> None:
    """Test function"""
    box = {
        "geometry": {
            "coordinates": [
                [[10, 10], [10, 20], [20, 20], [20, 10], [10, 10]]
            ]
        }
    }

    expected = np.array([10, 10, 20, 20], dtype = np.int32)
    result = format_box_prompt(box)

    np.testing.assert_array_equal(result, expected)


def test_format_box_prompt_invalid_structure() -> None:
    """Test function"""
    with pytest.raises(ValueError, match = "Invalid box format"):
        format_box_prompt({"geometry": {}})
