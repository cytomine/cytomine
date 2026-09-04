import numpy as np
import pytest
from PIL import Image as PILImage
from pyvips import Image as VIPSImage

from pims.processing.adapters import numpy_to_vips, pil_to_numpy, pil_to_vips, vips_to_numpy
from pims.utils.vips import vips_format_to_dtype


def test_numpy_to_vips():
    arr = np.arange(120, dtype=np.uint8).reshape((10, 4, 3))
    img = numpy_to_vips(arr)
    assert img.width == 4
    assert img.height == 10
    assert img.bands == 3

    arr = np.arange(40, dtype=np.uint8).reshape((10, 4))
    img = numpy_to_vips(arr)
    assert img.width == 4
    assert img.height == 10
    assert img.bands == 1

    with pytest.raises(ValueError):
        numpy_to_vips(np.arange(256, dtype=np.uint8).reshape((4, 4, 4, 4)))


def test_vips_to_numpy():
    img = VIPSImage.new_from_array([[1, 2, 3], [4, 5, 6]])
    arr = vips_to_numpy(img)
    h, w, d = arr.shape
    assert w == img.width
    assert h == img.height
    assert d == img.bands
    assert arr.dtype == vips_format_to_dtype[img.format]


def test_pil_to_numpy():
    img = PILImage.new("RGB", (20, 30))
    arr = pil_to_numpy(img)
    h, w, d = arr.shape
    assert w == 20
    assert h == 30
    assert d == 3


def test_pil_to_vips():
    img = PILImage.new("RGB", (20, 30))
    vips = pil_to_vips(img)
    assert vips.width == 20
    assert vips.height == 30
    assert vips.bands == 3
