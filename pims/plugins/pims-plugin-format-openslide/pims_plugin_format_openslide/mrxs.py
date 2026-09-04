from pathlib import Path

from pims.cache import cached_property
from pims.formats import AbstractFormat
from pims.formats.utils.abstract import CachedDataPath
from pims.formats.utils.checker import AbstractChecker
from pims.formats.utils.histogram import DefaultHistogramReader
from pims_plugin_format_openslide.utils.engine import OpenslideVipsParser, OpenslideVipsReader


def get_root_file(path: Path) -> Path | None:
    """Try to get MRXS main file (as it is a multi-file format)."""
    if path.is_dir():
        for child in path.iterdir():
            if child.suffix == '.mrxs':
                return child
    return None


class MRXSChecker(AbstractChecker):
    @classmethod
    def match(cls, pathlike: CachedDataPath) -> bool:
        root = get_root_file(pathlike.path)
        if root:
            d = root.parent / Path(root.stem)
            return d.is_dir() and (d / Path('Slidedat.ini')).exists()
        return False


class MRXSFormat(AbstractFormat):
    """
    3D Histech MRXS.

    References
        https://openslide.org/formats/mirax/
        https://github.com/openslide/openslide/blob/main/src/openslide-vendor-mirax.c

    """
    checker_class = MRXSChecker
    parser_class = OpenslideVipsParser
    reader_class = OpenslideVipsReader
    histogram_reader_class = DefaultHistogramReader

    def __init__(self, path, *args, **kwargs):
        super().__init__(path, *args, **kwargs)

        root = get_root_file(path)
        if root:
            self._path = root
            self.clear_cache()

        self._enabled = True

    @classmethod
    def get_name(cls):
        return "3D Histech MIRAX"

    @classmethod
    def get_remarks(cls):
        return "One .mrxs file and one directory with same name with .dat and .ini files, " \
               "packed in an archive. "

    @classmethod
    def is_spatial(cls):
        return True

    @cached_property
    def need_conversion(self):
        return False
