from functools import lru_cache
import numpy as np
from pylibCZIrw import czi
from struct import Struct
import czifile
from PIL import Image as PILImage
from io import BytesIO
from typing import Callable, List, Optional, Union
from pyvips import Image as VIPSImage
from pyvips import BandFormat

from pims_plugin_format_czi.czi_parser.czi_parser import CZIfile
from pims_plugin_format_czi.utils.area import ImageArea
from pims.formats.utils.histogram import DefaultHistogramReader
from pims.formats.utils.abstract import AbstractParser, AbstractReader, AbstractFormat, CachedDataPath
from pims.cache import cached_property
from pims.formats.utils.structures.metadata import ImageChannel, ImageMetadata
from pims.formats.utils.structures.pyramid import Pyramid
from pims.formats.utils.engines.tifffile import TifffileChecker
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


# Map the pixel format of image into Numpy types
PixelFormatToNPType = {
    "Gray8": np.uint8,
    "Gray16": np.uint16,
    "Gray32Float": np.float32,
    "Bgr24": np.uint8,
    "Bgr48": np.uint16,
    "Bgr96Float": np.float32
    }


PixelFormatToPIL = {
    "Gray8": "L",
    "Gray16": "L",
    "Bgr24": "RGB",
    "Bgr48": "RGB"
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
        czi_file  = cached_czi_file(self.format)
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
        
        imd.pixel_type = np.dtype(PixelFormatToNPType[czi_file.pixel_type])
        imd.significant_bits = dtype_to_bits(imd.pixel_type)

        return imd

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
        for img in czi_file.attachments():
            if img.attachment_entry.name == "Thumbnail":
                imd.associated_thumb.width = 10
                imd.associated_thumb.height = 10
                imd.associated_thumb.n_channels = 2
            if img.attachment_entry.name == "Label":
                imd.associated_label.width = 10
                imd.associated_label.height = 10
                imd.associated_label.n_channels = 2

        imd.physical_size_x = czi_file.width * czi_file.pixel_size[0]
        imd.physical_size_y = czi_file.height * czi_file.pixel_size[1]
        
        imd.objective.calibrated_magnification = czi_file.calibrated_magnification
        imd.acquisition_datetime = czi_file.acquisition_datetime
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
    """
    Other parser methods can be used, e.g. 'PyramidChecker' to fill a Pyramid
    object if the image file is a pyramid, to fill the acquisition date if needed, etc.
    """


class CZIReader(AbstractReader):

    def read_thumb(self, out_width, out_height, precomputed=None, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        for img in czi_file.attachments():
            if img.attachment_entry.name == "Thumbnail":
                thumbnail = img
                data = thumbnail.data()
                image_data = BytesIO(data)
                image = PILImage.open(image_data).convert(PixelFormatToPIL[czi_file.pixel_type])
                return image

        return True

    def read_label(self, out_width, out_height):
        czi_file_reader = cached_czi_file(self.format)
        for img in czi_file_reader.attachments():
                if img.attachment_entry.name == "Label":
                    label = img
                    data_raw = label.data(raw=True)
                    image_data = BytesIO(data_raw)
                    czi_embedded = czifile.CziFile(image_data)
                    data = czi_embedded.asarray()
                    data = data[0]
                    image = VIPSImage.new_from_memory(
                                data,
                                data.shape[1], data.shape[0],
                                data.shape[2],
                                format=numpy_to_vips_band_type[data.dtype]
                                )
                    return image


    def _mapzoom(self, level, nb_level):
        return (nb_level - level)/ nb_level
    
    def _mapcoords(self, coord, level, up_left_bounding_box):
        return int(coord*level + up_left_bounding_box)

    def read_window(self, region, out_width, out_height, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        self.format.pyramid = czi_file.pyramid
        tier = self.format.pyramid.most_appropriate_tier(
            region, (out_width, out_height)
        )
        region = region.scale_to_tier(tier)
        area = ImageArea.from_region(region)
        x_pos = self._mapcoords(area.coord[0], tier.level, czi_file.file_reader.total_bounding_box['X'][0])
        y_pos = self._mapcoords(area.coord[1], tier.level, czi_file.file_reader.total_bounding_box['Y'][0])
        roi = (x_pos, y_pos, int(area.size[0]), int(area.size[1]))
        zoom = self._mapzoom(tier.level, czi_file.pyramid.n_levels)
        data = czi_file.file_reader.read(roi=roi, zoom=zoom)
        window = PILImage.fromarray(data.astype(PixelFormatToNPType[czi_file.pixel_type]))
        return window

    def read_tile(self, tile, c=None, z=None, t=None):
        czi_file = cached_czi_file(self.format)
        self.format.pyramid = czi_file.pyramid
        tier = tile.tier
        zoom = self._mapzoom(tier.level, czi_file.pyramid.n_levels)    
        x_pos = self._mapcoords(tile.tx, tier.level, czi_file.file_reader.total_bounding_box['X'][0])
        y_pos = self._mapcoords(tile.ty, tier.level, czi_file.file_reader.total_bounding_box['Y'][0])
        roi=(x_pos, y_pos, tile.height, tile.width)
        data = czi_file.file_reader.read(roi=roi, zoom=zoom)
        image = PILImage.fromarray(data.astype(PixelFormatToNPType[czi_file.pixel_type]))
        return image


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
