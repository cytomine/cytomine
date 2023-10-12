from functools import lru_cache
import numpy as np
from struct import Struct
from pyvips import BandFormat

from pims_plugin_format_czi.czi_parser.czi_parser import CZIfile
from pims.formats.utils.histogram import DefaultHistogramReader
from pims.formats.utils.abstract import AbstractParser, AbstractReader, AbstractFormat, CachedDataPath
from pims.cache import cached_property
from pims.formats.utils.structures.metadata import ImageChannel, ImageMetadata
from pims.formats.utils.structures.pyramid import Pyramid
from pims.formats.utils.engines.tifffile import TifffileChecker
from pims.utils import UNIT_REGISTRY
from pims.utils.dtypes import dtype_to_bits

import logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger("pims.format.czi")


@lru_cache
def read_czifile(path, silent_fail=True):
    logger.debug(f"Read CZI file {path}")
    czi_file = CZIfile(str(path))
    return czi_file


def cached_czi_file(format: AbstractFormat):
    return format.get_cached(
        '_czi', read_czifile, format.path.resolve(), silent_fail=True
    )


class CZIChecker(TifffileChecker):

    MAGIC_WORD = b'ZISRAWFILE'

    @classmethod
    def match(cls, pathlike: CachedDataPath) -> bool:
        try:
            buf = cls.get_signature(pathlike)
            if len(buf) >= 10:
                magic_word_unpacker = Struct("10s")
                (magic_word,) = magic_word_unpacker.unpack(buf[0:10])
                logger.debug(f"CZI magic word found {magic_word}")
                return magic_word == CZIChecker.MAGIC_WORD
        except RuntimeError:
            return False


numpy_to_vips_band_type = {
    np.dtype(np.int8): BandFormat.CHAR,
    np.dtype(np.uint8): BandFormat.UCHAR,
    np.dtype(np.int16): BandFormat.SHORT,
    np.dtype(np.uint16): BandFormat.USHORT,
    np.dtype(np.int32): BandFormat.INT,
    np.dtype(np.uint32): BandFormat.UINT,
    np.dtype(np.float32): BandFormat.FLOAT,
    np.dtype(np.float64): BandFormat.DOUBLE,
    }


class CZIParser(AbstractParser):

    def _flattendict(self, d, parentkey='', sep='_'):
        items = []
        for k, v in d.items():
            newkey = parentkey + sep + k if parentkey else k
            if isinstance(v, dict):
                items.extend(self._flattendict(v, newkey, sep=sep).items())
            else:
                items.append((newkey, v))
        return dict(items)

    def parse_main_metadata(self) -> ImageMetadata:
        """
        File data necessary for PIMS to work (e.g. image size, pixel type, etc.).
        The information is contained in an ImageMetadata object (see the implementation
        of this object to know the needed information for PIMS).

        Returns the ImageMetadata object.
        """

        imd = ImageMetadata()
        czi_file = cached_czi_file(self.format)
        imd.width = czi_file.width
        imd.height = czi_file.height
        imd.n_concrete_channels = czi_file.n_concrete_channels
        imd.n_samples = 1
        if imd.n_concrete_channels == 1:
            imd.set_channel(ImageChannel(
                    index=0,
                    suggested_name="G"))
        else:
            names = ['B', 'G', 'R', 'A']
            for cc_idx in range(imd.n_concrete_channels):
                imd.set_channel(ImageChannel(
                    index=cc_idx,
                    suggested_name=names[cc_idx]))
        imd.depth = czi_file.depth
        imd.duration = czi_file.duration

        imd.pixel_type = np.dtype(czi_file.PixelFormatToNPType[czi_file.pixel_type])
        imd.significant_bits = dtype_to_bits(imd.pixel_type)

        return imd

    @staticmethod
    def _parse_physical_size(physical_size):
        """
        Convert the pixel size into physical size, CZI documentations tells that  is usually um
        """
        physical_quantity = UNIT_REGISTRY.Quantity('micrometer')
        return physical_size * physical_quantity
    
    def parse_known_metadata(self):
        """
        File data used in Cytomine but not necessary for PIMS (e.g. physical_size,
        magnification, ...)

        Returns an ImageMetadata object.

        Note that for the physical_size_{dimension} property, it is needed to specify
        the unit. Ex: imd.physical_size_x = 0.25*UNIT_REGISTRY("micrometers")
        """
        imd = ImageMetadata()
        czi_file = cached_czi_file(self.format)
        if czi_file.thumbnail_image is not None:
            imd.associated_thumb.width = czi_file.thumbnail_image.width
            imd.associated_thumb.height = czi_file.thumbnail_image.height
            imd.associated_thumb.n_channels = czi_file.thumbnail_image.n_components
        if czi_file.label_image is not None:
            imd.associated_label.width = czi_file.label_image.width
            imd.associated_label.height = czi_file.label_image.height
            imd.associated_label.n_channels = czi_file.label_image.n_components
        if czi_file.macro_image is not None:
            imd.associated_macro.width = czi_file.macro_image.width
            imd.associated_macro.height = czi_file.macro_image.height
            imd.associated_macro.n_channels = czi_file.macro_image.n_components          
        if czi_file.physical_pixel_size is not None:
            imd.physical_size_x = self._parse_physical_size(czi_file.physical_pixel_size[0])
            imd.physical_size_y = self._parse_physical_size(czi_file.physical_pixel_size[1])

        imd.objective.calibrated_magnification = czi_file.calibrated_magnification
        imd.objective.nominal_magnification = czi_file.calibrated_magnification
        imd.acquisition_datetime = czi_file.acquisition_datetime
        imd.microscope.model = czi_file.device_model
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
        czi_file = cached_czi_file(self.format)
        all_metadata = czi_file.raw_metadata
        all_metadata_dict_flat = self._flattendict(all_metadata)
        for k, v in all_metadata_dict_flat.items():
            imd.set(k, v, namespace="CZI")
        return imd

    def parse_pyramid(self) -> Pyramid:
        """
        Return the pyramid for the main image
        """

        czi_file = cached_czi_file(self.format)
        return czi_file.pyramid


class CZIReader(AbstractReader):

    def read_thumb(self, out_width, out_height, precomputed=None, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        return czi_file.thumbnail_image.read()

    def read_label(self, out_width, out_height):
        czi_file = cached_czi_file(self.format)
        return czi_file.label_image.read()

    def read_macro(self, out_width, out_height):
        czi_file = cached_czi_file(self.format)
        if czi_file.macro_image is not None:
            return czi_file.macro_image.read()
        else:
            return czi_file.overview_image.read()

    def read_window(self, region, out_width, out_height, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        tier = self.format.pyramid.most_appropriate_tier(
            region, (out_width, out_height)
        )
        region = region.scale_to_tier(tier)
        return czi_file.read_area(region.left, region.top, region.width, region.height, tier.level)

    def read_tile(self, tile, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        x = tile.tx * czi_file.tile_size[0]
        y = tile.ty * czi_file.tile_size[1]
        return czi_file.read_area(x, y, czi_file.tile_size, tile.tier.level)


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
