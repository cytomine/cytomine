import numpy as np
from skimage import dtype_limits
from skimage.exposure.exposure import _offset_array  # noqa


def to_unsigned_int(arr: np.ndarray) -> np.ndarray:
    """
    Offset the array to get the lowest value at 0 if there is any negative.
    """
    if arr.dtype is not np.uint8 or arr.dtype is not np.uint16:
        arr_min, arr_max = dtype_limits(arr, clip_negative=False)
        arr = _offset_array(arr, arr_min, arr_max)
    return arr
