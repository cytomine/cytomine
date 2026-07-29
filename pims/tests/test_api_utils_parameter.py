import os

import pytest

from pims.files.file import Path


@pytest.mark.parametrize("filepath", ("/abc", "abc", "abc/foo"))
def test_filepath2path(app, settings, filepath):
    assert str(Path.from_filepath(filepath)) == os.path.join(settings.root, filepath)


@pytest.mark.parametrize("rootpath", ("/abc", "abc", "abc/foo"))
def test_path2filepath(app, settings, rootpath):
    path = Path.from_filepath(rootpath)
    assert path.public_filepath == rootpath
