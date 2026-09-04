import numpy as np

from pims.formats.utils.structures.planes import PlanesInfo


def test_plane_info():
    pi = PlanesInfo(3, 5, 1, ['index'], [np.int64])
    pi.set(0, 0, 0, index=2)
    assert pi.get(0, 0, 0, 'index') == 2
    assert pi.get(0, 0, 0, 'invalid') is None

    pi = PlanesInfo(3, 5, 1)
    assert pi.get(0, 0, 0, 'invalid') is None
