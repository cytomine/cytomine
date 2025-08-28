"""Module to compute the patch of the image to extract."""

from math import floor
from typing import Tuple

import numpy as np
from cytomine.models import ImageInstance

ZOOM_OUT_FACTOR = 2.0
MAX_SIZE = 1024


def get_localisation_of_annotation(box: np.ndarray) -> Tuple[int, int, int, int]:
    """
    Function to get the position of the annotation with the top left corner, width and height.

    Args:
        (box: np.ndarray): the box prompt in format [x_min, y_min, x_max, y_max].

    Returns:
        Tuple of:
            - (int): width.
            - (int): height.
            - (int): x of top left corner.
            - (int): y of top left corner.
    """
    width = floor(box[2] - box[0])
    height = floor(box[3] - box[1])

    return width, height, floor(box[0]), floor(box[3]) # bottom left referential here


def get_annotation_size(img_width: int, img_height: int, width: int, height: int) -> int:
    """
    Function to get the size of the annotation to get considering the zoom-out factor.

    Args:
        (img_width: int): width of the image.
        (img_height: int): height of the image.
        (width: int): width of the annotation.
        (height: int): height of the annotation.

    Returns:
        (int): Returns the final size of the annotation to extract.
    """
    annotation_size = max(width, height)

    return min(floor(annotation_size * ZOOM_OUT_FACTOR), img_width, img_height)


def get_roi_around_annotation(img : ImageInstance, box: np.ndarray) -> Tuple[int, int, int, int]:
    """
    Function to get the position of the annotation to extract with the top left corner,
    width and height.

    With this function, the zoom-out factor has been taken into account.

    Args:
        (img : ImageInstance): the image instance from which the patch will be extracted.
        (box: np.ndarray): the box prompt in format [x_min, y_min, x_max, y_max].

    Returns:
        Tuple of:
            - (int): x of top left corner.
            - (int): y of top left corner.
            - (int): width.
            - (int): height.
    """
    annotation_width, annotation_height, x, y = get_localisation_of_annotation(box)
    size = get_annotation_size(img.width, img.height, annotation_width, annotation_height)

    h = (size - annotation_width) / 2
    v = (size - annotation_height) / 2
    x = floor(x - h)
    y = floor(img.height - y - v)

    x = min(x, img.width - size)
    y = min(y, img.height - size)
    x = max(0, x)
    y = max(0, y)

    return x, y, size, size
