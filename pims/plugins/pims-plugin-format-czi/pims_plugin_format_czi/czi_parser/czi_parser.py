import czifile
import logging
from math import ceil, log2
import numpy as np
from PIL import Image as PILImage
from pylibCZIrw import czi

from pims.formats.utils.structures.pyramid import Pyramid

logger = logging.getLogger("pims.format.czi")


class DictObj:
    """Create an object based on a dictionary
    """

    def __init__(self, in_dict: dict):
        assert isinstance(in_dict, dict)
        for key, val in in_dict.items():
            if isinstance(val, (list, tuple)):
                setattr(self, key, [DictObj(x) if isinstance(x, dict) else x for x in val])
            else:
                setattr(self, key, DictObj(val) if isinstance(val, dict) else val)


class CZIfile():

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

    def __init__(self, path):

        self._czi_file_reader = czi.CziReader(path)
        self._thumbnails_reader = czifile.CziFile(path)

        bounding_box = self._czi_file_reader.total_bounding_box
        self._width = bounding_box['X'][1] - bounding_box['X'][0]
        self._height = bounding_box['X'][1] - bounding_box['X'][0]

        if 'C' in bounding_box:
            self._n_concrete_channels = bounding_box['C'][1] - bounding_box['C'][0]
        else:
            self._n_concrete_channels = 1
        if 'Z' in bounding_box:
            self._depth = bounding_box['Z'][1] - bounding_box['Z'][0]
        else:
            self._depth = 1
        if 'T' in bounding_box:
            self._duration = bounding_box['T'][1] - bounding_box['T'][0]
        else:
            self._duration = 1

        self._pixel_type = self._czi_file_reader.pixel_types[0]
        self._attachments = self._thumbnails_reader.attachments()

        self._raw_metadata = self._czi_file_reader.metadata
        self._metadata_dict_obj = DictObj(self._raw_metadata)

        if hasattr(self._metadata_dict_obj.ImageDocument.Metadata, 'ImageScaling'):
            physical_pixel_size = self._metadata_dict_obj.ImageDocument.Metadata.ImageScaling.ImagePixelSize
            self._pixel_size_x = float(physical_pixel_size.split(',')[0])
            self._pixel_size_y = float(physical_pixel_size.split(',')[1])
        else:
            self._pixel_size_x = None
            self._pixel_size_y = None

        if self._metadata_dict_obj.ImageDocument.Metadata.Scaling is not None:
            self._calibrated_magnification = self._metadata_dict_obj.ImageDocument.Metadata.Scaling.AutoScaling.CameraAdapterMagnification
            self._acquisition_datetime = self._metadata_dict_obj.ImageDocument.Metadata.Information.Image.AcquisitionDateAndTime

        self._tile_size = (256, 256)
        self._pyramid = self._create_pyramid()

    @property
    def file_reader(self) -> float:
        """
        Return the CZI file obtained from pylibCZIrw lib
        """

        return self._czi_file_reader

    @property
    def file_thumbnails_reader(self) -> float:
        """
        Return the CZI file obtained from czifile lib
        """

        return self._thumbnails_reader

    @property
    def width(self) -> float:
        """
        Return the Image width in pixels
        """

        return self._width

    @property
    def height(self) -> float:
        """
        Return the Image height in pixels
        """

        return self._height

    @property
    def n_concrete_channels(self) -> float:
        """
        Return the Image concrete channels
        """

        return self._n_concrete_channels

    @property
    def depth(self) -> float:
        """
        Return the Image z dimension
        """

        return self._depth

    @property
    def duration(self) -> float:
        """
        Return the Image acquisition duration
        """

        return self._duration

    @property
    def pixel_type(self) -> float:
        """
        Return the Image pixel type
        """

        return self._pixel_type

    def attachments(self):
        """
        Return the file attachments
        """

        return self._thumbnails_reader.attachments()

    @property
    def raw_metadata(self) -> dict:
        """
        Return the file metadata in raw format
        """

        return self._raw_metadata

    @property
    def metadata_object(self) -> float:
        """
        Return the file metadata as an actual object
        """

        return self._metadata_dict_obj

    @property
    def pixel_size(self) -> (float, float):
        """
        Return the Image Pixel size
        """

        return (self._width, self._height)

    @property
    def physical_pixel_size(self) -> (float, float):
        """
        Return the Pixel Physical size
        """

        return (self._pixel_size_x, self._pixel_size_y)

    @property
    def tile_size(self) -> (int, int):
        """
        Return the size of a tile of the pyramid
        """

        return self._tile_size

    @property
    def acquisition_datetime(self) -> float:
        """
        Return the image acquisition datetime
        """

        return self._acquisition_datetime

    @property
    def calibrated_magnification(self) -> float:
        """
        Return the maximum calibrated magnification that will be used in PIMS
        """

        return self._calibrated_magnification

    def _create_pyramid(self):
        """
        """
        logger.debug("Creating pyramid")
        pyramid = Pyramid()
        pixel_size_x = self.pixel_size[0]
        pixel_size_y = self.pixel_size[1]
        width, height = pixel_size_x, pixel_size_y
        max_order = max(
            ceil(log2((pixel_size_x + self.tile_size[0] - 1) // self.tile_size[0])),
            ceil(log2((pixel_size_y + self.tile_size[1] - 1) // self.tile_size[1])))
        nb_levels = max_order + 1
        logger.debug(f"Add level 0 of size {(width, height)}")
        pyramid.insert_tier(width, height, (self.tile_size[0], self.tile_size[1]))
        for level in range(1, nb_levels):
            # Stick to the way the pyramid tier are created in PIMS to avoid any difference between level calculation
            width, height = round(width / 2), round(height / 2)
            logger.debug(f"Add level {level} of size {(width, height)}")
            pyramid.insert_tier(width, height, (self.tile_size[0], self.tile_size[1]))
        return pyramid

    @property
    def pyramid(self) -> float:
        """
        Return Image Pyramid
        """

        return self._pyramid

    def read_area(self, x: int, y: int, width: int, height: int, level: int) -> PILImage:
        """
        Return the image of the given area at the requested zoom level.
        """

        scale = 2 ** level
        box = self.file_reader.total_bounding_box
        x_pos = x * scale + box['X'][0]
        y_pos = y * scale + box['Y'][0]
        roi = (x_pos, y_pos, width * scale, height * scale)

        zoom = (2 ** (self.pyramid.n_levels - level)) / (2 ** self.pyramid.n_levels)

        data = self.file_reader.read(roi=roi, zoom=zoom)
        image = PILImage.fromarray(data.astype(self.PixelFormatToNPType[self.pixel_type]))
        return image
