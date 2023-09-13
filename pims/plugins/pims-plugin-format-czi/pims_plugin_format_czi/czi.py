from pims.formats.utils.histogram import DefaultHistogramReader
from pims.formats.utils.abstract import AbstractParser, AbstractReader, AbstractFormat, CachedDataPath
from pims.cache import cached_property
from pims.formats.utils.structures.metadata import ImageMetadata
from pims.formats.utils.engines.tifffile import TifffileChecker



import logging
logging.basicConfig(level=logging.DEBUG)


class CZIChecker(TifffileChecker):

    @classmethod
    def match(cls, pathlike: CachedDataPath) -> bool:
            return True


class CZIParser(AbstractParser):

    def parse_main_metadata(self) -> ImageMetadata:
        """
        File data necessary for PIMS to work (e.g. image size, pixel type, etc.).
        The information is contained in an ImageMetadata object (see the implementation
        of this object to know the needed information for PIMS).

        Returns the ImageMetadata object.
        """
        imd = ImageMetadata()
        return imd

    def parse_known_metadata(self):
        """
        File data used in Cytomine but not necessary for PIMS (e.g. physical_size,
        magnification, ...)

        Returns an ImageMetadata object.

        Note that for the physical_size_{dimension} property, it is needed to specify
        the unit. Ex: imd.physical_size_x = 0.25*UNIT_REGISTRY("micrometers")
        """
        imd = super().parse_known_metadata()
        return imd

    def parse_raw_metadata(self):
        """
        Additional information that is not useful either for PIMS or Cytomine.
        Information used when the URL "http://localhost/image/{filepath}/metadata"
        is fetched.

        Returns a MetadataStore object.

        """

        """
        To fill this MetadataStore object with new image properties, use
        - key for the image property (e.g. model name, calibration time, etc.)
        - value for the value of the image property
        -> store.set(key, value)
        """
        imd = super().parse_raw_metadata()
        return imd
    """
    Other parser methods can be used, e.g. 'PyramidChecker' to fill a Pyramid
    object if the image file is a pyramid, to fill the acquisition date if needed, etc.
    """


class CZIReader(AbstractReader):

    def read_thumb(self, out_width, out_height, precomputed=None, c=None, z=None, t=None):
        return True

    def read_window(self, region, out_width, out_height, c=None, z=None, t=None):
        return True

    def read_tile(self, tile, c=None, z=None, t=None):
        return True


class CZIFormat(AbstractFormat):
    checker_class = CZIChecker
    parser_class = CZIParser
    reader_class = CZIReader
    histogram_reader_class = DefaultHistogramReader

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._enabled = True

    @classmethod
    def get_name(cls):
        return "Zeiss CZI"

    @classmethod
    def get_remarks(cls):
        return "A set of .CZI files packed in archive directory."

    @classmethod
    def is_spatial(cls):
        return True

    @cached_property
    def need_conversion(self):
        return False
