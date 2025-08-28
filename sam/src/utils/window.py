"""Module to extract a patch of the original WSI image."""

import os
import tempfile
from typing import Union

import numpy as np
import matplotlib.pyplot as plt

from cytomine.models.image import ImageInstance


def load_cytomine_window_image(obj: ImageInstance,
                               x: int,
                               y: int,
                               w: int,
                               h: int,
                               max_size: Union[int, None] = None
    ) -> Union[np.ndarray, None]:
    """
    Function to download the cropped part of the original WSI.

    Args:
        (obj: ImageInstance): the original WSI Image Instance.
        (x: int): the offset along the x-axis.
        (y: int): the offset along the y-axis.
        (w: int): the width of the cropped part.
        (h: int): the height of the cropped part.
        (max_size: Union[int, None]): Optional max size for 
                         resizing window (resizes output image).

    Returns:
        (np.ndarray): Returns the cropped part of the image
    """
    with tempfile.TemporaryDirectory() as tmpdir:
        tmp_path = os.path.join(tmpdir, "window.jpg")

        if max_size is None:
            obj.window(x, y, w, h, dest_pattern = tmp_path)

        else:
            obj.window(x, y, w, h, dest_pattern = tmp_path, max_size = max_size)

        try:
            return plt.imread(tmp_path).copy()

        except Exception as _:
            return None
