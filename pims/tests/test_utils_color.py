import numpy as np

from pims.utils.color import Color, np_int2rgb


def test_rgb_int_conversion():
    assert np.array_equal(
        Color(Color((10, 255, 0)).as_int()).as_rgb_tuple(alpha=False),
        (10, 255, 0)
    )


def test_rgba_int_conversion():
    assert np.allclose(
        Color(Color((10, 255, 0, 0.5)).as_int()).as_rgb_tuple(alpha=True),
        (10, 255, 0, 0.5),
        atol=1e-2
    )


def test_np_int2rgb():
    a = np_int2rgb(np.array([1684300800]))
    assert np.array_equal(a, np.array([100, 100, 100]))
