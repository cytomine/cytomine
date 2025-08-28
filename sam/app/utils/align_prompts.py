"""Module to align the prompts in the new referential."""

import numpy as np


def align_box_prompt(
        box: np.ndarray,
        x_tl: int,
        y_tl: int,
        img_height: int,
        scale_x: float,
        scale_y: float
    ) -> np.ndarray:
    """
    Function to align the box prompt with the extracted window (the coordinates of the box prompt
    must be relative to the window and not to the WSI, this function performs this transformation).

    Args:
        (box: np.ndarray): the box prompt with format: np.array([x_min, y_min, x_max, y_max]).
        (x_tl: int): the offset along the x-axis.
        (y_tl: int): the offset along the y-axis.
        (img_height: int): the height of the image.
        (scale_x: float): the scaling factor to apply to x coordinates.
        (scale_y: float): the scaling factor to apply to y coordinates.

    Returns:
        (np.ndarray): Returns the transformed box prompt.
    """
    x_min, y_min, x_max, y_max = box # bottom left referential

    y_min_flipped = img_height - y_max
    y_max_flipped = img_height - y_min

    new_x_min = (x_min - x_tl) * scale_x
    new_x_max = (x_max - x_tl) * scale_x
    new_y_min = (y_min_flipped - y_tl) * scale_y
    new_y_max = (y_max_flipped - y_tl) * scale_y

    return np.array([new_x_min, new_y_min, new_x_max, new_y_max], dtype = np.int32)


def align_point_prompt(
        points: np.ndarray,
        x_tl: int,
        y_tl: int,
        img_height: int,
        scale_x: float,
        scale_y: float
    ) -> np.ndarray:
    """
    Function to align the point prompts with the extracted window (the coordinates of
    the point promptsmust be relative to the window and not to the WSI, this function
    performs this transformation).

    Args:
        (points: np.ndarray): the point prompts.
        (x_tl: int): the offset along the x-axis.
        (y_tl: int): the offset along the y-axis.
        (img_height: int): the height of the image.
        (scale_x: float): the scaling factor to apply to x coordinates.
        (scale_y: float): the scaling factor to apply to y coordinates.

    Returns:
        (np.ndarray): Returns the transformed point prompts.
    """
    for p in points: # p[0] = x, p[1] = y (bottom-left referential)
        x, y = p[0], p[1]

        y_flipped = img_height - y

        p[0] = int((x - x_tl) * scale_x)
        p[1] = int((y_flipped - y_tl) * scale_y)

    return points
