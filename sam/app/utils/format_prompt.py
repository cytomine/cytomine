"""Module to format prompt from the GeoJSON format to SAM format."""

from typing import List, Dict, Any, Union, Tuple
import numpy as np


def format_point_prompt(points_data: List[Dict[str, Any]]
                        ) -> Tuple[Union[np.ndarray, None], Union[np.ndarray, None]]:
    """
    Function to format the point prompts from the GeoJSON format to the SAM format.

    Args:
        (points_data: List[Dict[str, Any]]): the list of point prompts.

    Returns:
        Tuple of:
            - (np.ndarray): the formatted point prompt coordinates (Shape: Nx2).
            - (np.ndarray): the formatted point prompt labels (Shape: N).
    """
    if not points_data:
        return None, None

    try:
        point_coords = np.array(
            [list(map(int, pt["geometry"]["coordinates"])) for pt in points_data],
            dtype = np.int32
        )

        point_labels = np.array(
            [int(pt["properties"]["label"]) for pt in points_data],
            dtype = np.int32
        )

        return point_coords, point_labels

    except Exception as e:
        raise ValueError(f"Invalid point data format: {e}") from e


def format_box_prompt(box: Dict[str, Any]) -> np.ndarray:
    """
    Function to format the box prompt from the GeoJSON format to the SAM format.

    Args:
        (box: Dict[str, Any]): the box prompt.

    Returns:
        (np.ndarray): Returns the formatted box prompt in format [x_min, y_min, x_max, y_max].
    """
    try:
        coordinates = box["geometry"]["coordinates"]

        coords = np.array(coordinates[0][:4], dtype = np.int32) # to ignore the closing point

        x_min = np.min(coords[:, 0])
        y_min = np.min(coords[:, 1])
        x_max = np.max(coords[:, 0])
        y_max = np.max(coords[:, 1])

        return np.array([x_min, y_min, x_max, y_max], dtype = np.int32)

    except Exception as e:
        raise ValueError(f"Invalid box format: {e}") from e
