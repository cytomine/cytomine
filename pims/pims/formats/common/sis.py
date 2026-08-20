from pims.cache import cached_property

from pims.formats.utils.abstract import AbstractFormat, CachedDataPath
from pims.formats.utils.engines.tifffile import TifffileChecker, TifffileParser, cached_tifffile
from pims.formats.utils.engines.vips import VipsReader, VipsSpatialConvertor
from pims.formats.utils.histogram import DefaultHistogramReader
from pims.formats.utils.structures.metadata import ImageMetadata, MetadataStore
from pims.formats.utils.structures.planes import PlanesInfo
from pims.utils import UNIT_REGISTRY


class OlympusSisChecker(TifffileChecker):
    @classmethod
    def match(cls, pathlike: CachedDataPath) -> bool:
        try:
            if not super().match(pathlike):
                return False

            tf = cls.get_tifffile(pathlike)
            return tf.is_sis
        except RuntimeError:
            return False


class OlympusSisParser(TifffileParser):
    @cached_property
    def _parsed_sis_metadata(self) -> dict:
        return cached_tifffile(self.format).sis_metadata

    def parse_known_metadata(self) -> ImageMetadata:
        imd = super().parse_known_metadata()
        sis_metadata = self._parsed_sis_metadata

        imd.acquisition_datetime = sis_metadata.get('datetime')
        
        physical_size_x = sis_metadata.get('pixelsizex')
        if physical_size_x is not None and physical_size_x > 0:
            imd.physical_size_x = physical_size_x * UNIT_REGISTRY('meters')
        physical_size_y = sis_metadata.get('pixelsizey')
        if physical_size_y is not None and physical_size_y > 0:
            imd.physical_size_y = physical_size_y * UNIT_REGISTRY('meters')

        imd.objective.nominal_magnification = sis_metadata.get('magnification')
        return imd

    def parse_raw_metadata(self) -> MetadataStore:
        store = super().parse_raw_metadata()

        for key, value in self._parsed_sis_metadata.items():
            store.set(key, value, namespace="OLYMPUS")
        return store

    def parse_planes(self) -> PlanesInfo:
        return super().parse_planes()


class OlympusSisFormat(AbstractFormat):
    """
    Known limitations:
    * Do not consider images with depth, time or several bands (except rgb)
    """
    checker_class = OlympusSisChecker
    parser_class = OlympusSisParser
    reader_class = VipsReader
    histogram_reader_class = DefaultHistogramReader
    convertor_class = VipsSpatialConvertor

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._enabled = True

    @classmethod
    def get_name(cls):
        return "Olympus SIS TIFF"

    @classmethod
    def _get_identifier(cls):
        return "SIS"

    @classmethod
    def is_spatial(cls):
        return True

    @cached_property
    def need_conversion(self):
        imd = self.main_imd
        return imd.width > 1024 or imd.height > 1024
