from collections import OrderedDict
from functools import lru_cache
import numpy as np
from operator import itemgetter
from pyvips import Image as VIPSImage
from struct import Struct
from typing import Callable, List, Optional, Union

from pims_plugin_format_czi.czi_parser.czi_file import CZIFile
from pims.formats.utils.histogram import DefaultHistogramReader
from pims.formats.utils.abstract import AbstractParser, AbstractReader, AbstractFormat, CachedDataPath
from pims.cache import cached_property
from pims.formats.utils.structures.metadata import ImageChannel, ImageMetadata, MetadataStore
from pims.formats.utils.structures.pyramid import Pyramid
from pims.formats.utils.engines.tifffile import TifffileChecker
from pims.processing.adapters import RawImagePixels
from pims.processing.region import Region, Tile
from pims.utils import UNIT_REGISTRY
from pims.utils.color import infer_channel_color
from pims.utils.dtypes import dtype_to_bits
from pims.utils.vips import bandjoin, fix_rgb_interpretation

import logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger("pims.format.czi")


@lru_cache
def read_czifile(path, silent_fail=True):
    logger.debug(f"Read CZI file {path}")
    czi_file = CZIFile(str(path))
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


class CZIParser(AbstractParser):

    def _flattendict(self, d, parentkey='', sep='_'):
        items = []
        for k, v in d.items():
            newkey = parentkey + sep + k if parentkey else k
            if isinstance(v, dict):
                items.extend(self._flattendict(v, newkey, sep=sep).items())
            elif isinstance(v, list):
                for i in range(len(v)):
                    new_list_key = newkey + '_' + str(i)
                    if isinstance(v[i], dict):
                        items.extend(self._flattendict(v[i], new_list_key, sep=sep).items())
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
        imd.pixel_type = np.dtype(czi_file.pixel_type)
        imd.n_samples = czi_file.num_components
        imd.significant_bits = dtype_to_bits(imd.pixel_type)
        imd.n_concrete_channels = czi_file.n_concrete_channels
        imd.depth = czi_file.depth
        imd.duration = czi_file.duration
        imd.frame_rate = czi_file.frame_rate

        for cc_idx in range(imd.n_concrete_channels):
            colors = [infer_channel_color(
                None,
                cc_idx,
                imd.n_concrete_channels
            )] * imd.n_samples

            if imd.n_samples == 3 and colors[0] is None:
                colors = [
                    infer_channel_color(None, i, 3)
                    for i in range(imd.n_samples - 1, -1, -1)
                ]

            names = [czi_file.channel_name(cc_idx)] * imd.n_samples
            if names[0] is None:
                if imd.n_samples == 1 and 2 <= imd.n_concrete_channels <= 3:
                    names = ['BGR'[cc_idx]]
                elif imd.n_samples == 3:
                    names = ['B', 'G', 'R']
            emission = czi_file.channel_emission_wavelength(cc_idx)
            excitation = czi_file.channel_excitation_wavelength(cc_idx)

            for s in range(imd.n_samples):
                imd.set_channel(ImageChannel(
                    index=cc_idx * imd.n_samples + s,
                    suggested_name=names[s],
                    color=colors[s],
                    emission_wavelength=emission,
                    excitation_wavelength=excitation

                ))

        return imd

    @staticmethod
    def _parse_physical_size(physical_size):
        """
        Convert the pixel size into physical size, CZI documentations tells that  is usually um
        """
        physical_quantity = UNIT_REGISTRY.Quantity('micrometer')
        return physical_size * physical_quantity

    def parse_known_metadata(self) -> ImageMetadata:
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

        imd.physical_size_z = czi_file.depth_scale

        imd.objective.calibrated_magnification = czi_file.calibrated_magnification
        imd.objective.nominal_magnification = czi_file.calibrated_magnification
        imd.acquisition_datetime = czi_file.acquisition_datetime
        imd.microscope.model = czi_file.device_model

        return imd

    def parse_raw_metadata(self) -> MetadataStore:
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

    @staticmethod
    def _extract_channels(im: VIPSImage, c: Optional[Union[int, List[int]]]) -> VIPSImage:
        if c is None or im.bands == len(c):
            return im
        elif type(c) is int or len(c) == 1:
            if len(c) == 1:
                c = c[0]
            return im.extract_band(c)
        else:
            channels = list(itemgetter(*c)(im))
            im = channels[0].bandjoin(channels[1:])
            return im

    def _multichannel_read(
            self,
            read_func: Callable[[int, Optional[int], Optional[int]], VIPSImage],
            c: Optional[Union[int, List[int]]] = None,
            z: Optional[int] = None,
            t: Optional[int] = None):
        """
        Internal method to perform a multi channel image composition. The actual image reading is performed by the
        given read_func callable parameter.
        """

        bands = []
        if z is None:
            z = 0
        if t is None:
            t = 0
        # Get the list of channels and samples to read
        cc_idxs, s_idxs = self._concrete_channel_indexes(c)
        # Aggregate the samples to read per concrete channel to not read multiple times the same image
        channels = OrderedDict()
        for cc_idx, s_idx in zip(cc_idxs, s_idxs):
            if cc_idx in channels:
                channels[cc_idx].append(s_idx)
            else:
                channels[cc_idx] = [s_idx]
        for channel, samples in channels.items():
            # Read the requested channel
            im = read_func(channel, z, t)
            if im.hasalpha():
                im = im.flatten()
            # If needed, extract the required samples from the image
            im = self._extract_channels(im, samples)
            bands.append(im)
        # If we have multiple channels, join them to create a single image
        result = bandjoin(bands)
        # Assign a proprer RGB color space if applicable
        if c == [0, 1, 2]:
            result = fix_rgb_interpretation(result)
        return result

    def read_thumb(
        self, out_width: int, out_height: int, precomputed: bool = None,
        c: Optional[Union[int, List[int]]] = None, z: Optional[int] = None, t: Optional[int] = None
    ) -> RawImagePixels:
        czi_file = cached_czi_file(self.format)
        if precomputed and czi_file.thumbnail_image is not None:
            return czi_file.thumbnail_image.read()
        else:
            return self._multichannel_read(
                lambda c, z, t: czi_file.read_area(0, 0, out_width, out_height, 0, c, z, t), c, z, t)

    def read_label(self, out_width: int, out_height: int) -> RawImagePixels:
        czi_file = cached_czi_file(self.format)
        return czi_file.label_image.read()

    def read_macro(self, out_width: int, out_height: int, c: Optional[Union[int, List[int]]] = None) -> RawImagePixels:
        czi_file = cached_czi_file(self.format)
        if czi_file.macro_image is not None:
            return czi_file.macro_image.read()
        else:
            return czi_file.overview_image.read()

    def read_window(
        self, region: Region, out_width: int, out_height: int,
        c: Optional[Union[int, List[int]]] = None, z: Optional[int] = None, t: Optional[int] = None
    ) -> RawImagePixels:
        czi_file = cached_czi_file(self.format)
        tier = self.format.pyramid.most_appropriate_tier(
            region, (out_width, out_height)
        )
        region = region.scale_to_tier(tier)
        return self._multichannel_read(
            lambda c, z, t: czi_file.read_area(
                region.left, region.top, region.width, region.height, tier.level, c, z, t), c, z, t)

    def read_tile(
        self, tile: Tile,
        c: Optional[Union[int, List[int]]] = None, z: Optional[int] = None, t: Optional[int] = None
    ) -> RawImagePixels:
        czi_file = cached_czi_file(self.format)
        x = tile.tx * czi_file.tile_size[0]
        y = tile.ty * czi_file.tile_size[1]
        return self._multichannel_read(
            lambda c, z, t: czi_file.read_area(x, y, *czi_file.tile_size, tile.tier.level, c, z, t), c, z, t)


class CZIFormat(AbstractFormat):
    checker_class = CZIChecker
    parser_class = CZIParser
    reader_class = CZIReader
    histogram_reader_class = DefaultHistogramReader

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._enabled = True

    @classmethod
    def get_name(cls) -> str:
        return "Zeiss CZI"

    @classmethod
    def is_spatial(cls) -> bool:
        return True

    @cached_property
    def need_conversion(self) -> bool:
        return False
