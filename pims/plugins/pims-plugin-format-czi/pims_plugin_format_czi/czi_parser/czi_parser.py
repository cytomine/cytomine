import czifile
from io import BytesIO
import logging
from math import ceil, log2
import numpy as np
from pims.formats.utils.structures.pyramid import Pyramid
from pylibCZIrw import czi
from pyvips import Image as VIPSImage
from pyvips import BandFormat
from typing import Optional


logger = logging.getLogger("pims.format.czi")

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


class CZIAttachment:

    def __init__(self, attachment):
        self.attachment = attachment


class CZIVIPSAttachment(CZIAttachment):

    def __init__(self, attachment):
        super().__init__(attachment)
        raw_data = attachment.data(raw=True)
        self._image = VIPSImage.new_from_buffer(raw_data, options="")

    @property
    def width(self):
        return self._image.width

    @property
    def height(self):
        return self._image.height

    @property
    def n_components(self):
        return self._image.bands

    def read(self) -> VIPSImage:
        return self._image


class CZINAtiveAttachment(CZIAttachment):

    def __init__(self, attachment):
        super().__init__(attachment)
        raw_data = attachment.data(raw=True)
        image_data = BytesIO(raw_data)
        self._czi = czifile.CziFile(image_data)

    @property
    def width(self):
        return self._czi.shape[2]

    @property
    def height(self):
        return self._czi.shape[1]

    @property
    def n_components(self):
        return self._czi.shape[0]

    def read(self) -> VIPSImage:
        data = self._czi.asarray()
        data = data[0]
        image = VIPSImage.new_from_memory(
            data,
            data.shape[1], data.shape[0],
            data.shape[2],
            format=numpy_to_vips_band_type[data.dtype],
            )
        return image


class CZIfile():

    # Map the pixel format of image into Numpy types
    PixelFormatToNPType = {
        "Gray8": np.uint8,
        "Gray16": np.uint16,
        "Gray32Float": np.float32,
        "Bgr24": np.uint8,
        "Bgr48": np.uint16,
        "Bgr96Float": np.float32,
        }

    PixelFormatToNumComponents = {
        "Gray8": 1,
        "Gray16": 1,
        "Gray32Float": 1,
        "Bgr24": 3,
        "Bgr48": 3,
        "Bgr96Float": 3,
        }

    PixelFormatToPIL = {
        "Gray8": "L",
        "Gray16": "L",
        "Gray32Float": "L",
        "Bgr24": "RGB",
        "Bgr48": "RGB",
        "Bgr96Float": "RGB",
        }

    numpy_to_vips_band_type = {
        np.int8: BandFormat.CHAR,
        np.uint8: BandFormat.UCHAR,
        np.int16: BandFormat.SHORT,
        np.uint16: BandFormat.USHORT,
        np.int32: BandFormat.INT,
        np.uint32: BandFormat.UINT,
        np.float32: BandFormat.FLOAT,
        np.float64: BandFormat.DOUBLE,
        }

    def __init__(self, path):

        self._czi_file_reader = czi.CziReader(path)
        self._attachment_reader = czifile.CziFile(path)

        self._thumbnail = None
        self._overview = None
        self._label = None
        self._macro = None

        bounding_box = self._czi_file_reader.total_bounding_box
        self._width = bounding_box['X'][1] - bounding_box['X'][0]
        self._height = bounding_box['X'][1] - bounding_box['X'][0]

        if 'C' in bounding_box:
            self._n_concrete_channels = bounding_box['C'][1] - bounding_box['C'][0]
            self._start_channels = bounding_box['C'][0]
        else:
            self._n_concrete_channels = 1
            self._start_channels = 0
        if 'Z' in bounding_box:
            self._depth = bounding_box['Z'][1] - bounding_box['Z'][0]
            self._start_depth = bounding_box['Z'][0]
        else:
            self._depth = 1
            self._start_depth = 0
        if 'T' in bounding_box:
            self._duration = bounding_box['T'][1] - bounding_box['T'][0]
            self._start_duration = bounding_box['T'][0]
        else:
            self._duration = 1
            self._start_duration = 0

        pixel_type = self._czi_file_reader.pixel_types[0]
        self._pixel_type = self.PixelFormatToNPType[pixel_type]
        self._num_components = self.PixelFormatToNumComponents[pixel_type]

        self._raw_metadata = self._czi_file_reader.metadata
        self._metadata_dict_obj = DictObj(self._raw_metadata)

        if hasattr(self._metadata_dict_obj.ImageDocument.Metadata, 'ImageScaling'):
            physical_pixel_size = self._metadata_dict_obj.ImageDocument.Metadata.ImageScaling.ImagePixelSize
            self._pixel_size_x = float(physical_pixel_size.split(',')[0])
            self._pixel_size_y = float(physical_pixel_size.split(',')[1])
        elif hasattr(self._metadata_dict_obj.ImageDocument.Metadata, 'Scaling'):
            if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Scaling, 'AutoScaling'):
                physical_pixel_size = self._metadata_dict_obj.ImageDocument.Metadata.Scaling.AutoScaling.CameraPixelDistance
                self._pixel_size_x = float(physical_pixel_size.split(',')[0])
                self._pixel_size_y = float(physical_pixel_size.split(',')[1])
            else:
                self._pixel_size_x = 0
                self._pixel_size_y = 0
        else:
            self._pixel_size_x = 0
            self._pixel_size_y = 0

        if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information, 'Instrument'):
            calibrated_magnification = 0
            nominal_magnification = 0
            if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument, 'Objectives'):
                if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Objectives.Objective, 'CalibratedMagnification'):
                    calibrated_magnification = float(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Objectives.Objective.CalibratedMagnification)
                if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Objectives.Objective, 'NominalMagnification'):
                    nominal_magnification = float(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Objectives.Objective.NominalMagnification)
                self._calibrated_magnification = max(calibrated_magnification, nominal_magnification)
        else:
            self._calibrated_magnification = 0

        if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information, 'Image'):
            if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Image, 'AcquisitionDateAndTime'):
                self._acquisition_datetime = self._metadata_dict_obj.ImageDocument.Metadata.Information.Image.AcquisitionDateAndTime
            else:
                self._acquisition_datetime = 0
        else:
            self._acquisition_datetime = 0

        if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information, 'Instrument'):
            if hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument, 'Microscopes'):
                self._device_model = getattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Microscopes.Microscope, '@Name')
            elif hasattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument, 'Objectives'):
                self._device_model = getattr(self._metadata_dict_obj.ImageDocument.Metadata.Information.Instrument.Objectives.Objective, '@Name')
            else:
                self._device_model = "Zeiss Microscope"
        else:
            self._device_model = "Zeiss Microscope"

        self._channel_names = [None for _ in range(self._n_concrete_channels)]

        self._analyze_images()

        self._tile_size = (256, 256)
        self._pyramid = self._create_pyramid()

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

    def channel_name(self, index: int) -> Optional[str]:
        """
        Return the name of the channel, if known
        """

        if index < self._n_concrete_channels:
            return self._channel_names[index]
        else:
            return None

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

    @property
    def num_components(self) -> float:
        """
        Return the number of components per pixel
        """

        return self._num_components

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

    @property
    def device_model(self) -> str:
        """
        Return the sed microscope model
        """

        return self._device_model

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

    def read_area(self, x: int, y: int, width: int, height: int, level: int, c, z, t) -> VIPSImage:
        """
        Return the image of the given area at the requested zoom level.
        """

        scale = 2 ** level
        box = self._czi_file_reader.total_bounding_box
        x_pos = x * scale + box['X'][0]
        y_pos = y * scale + box['Y'][0]
        roi = (x_pos, y_pos, width * scale, height * scale)

        zoom = (2 ** (self.pyramid.n_levels - level)) / (2 ** self.pyramid.n_levels)

        plane = {"C": self._start_channels + c, "Z": self._start_depth + z, "T": self._start_duration + t}
        data = self._czi_file_reader.read(roi=roi, zoom=zoom, plane=plane)
        image = VIPSImage.new_from_memory(
            np.ascontiguousarray(data),
            width,
            height,
            self.num_components,
            format=self.numpy_to_vips_band_type[self.pixel_type]
            )
        return image

    def _analyze_images(self) -> None:
        for img in self._attachment_reader.attachments():
            if img.attachment_entry.name == "Thumbnail":
                self._thumbnail = self.image_from_attachment(img)
            elif img.attachment_entry.name == "SlideOverview":
                self._overview = self.image_from_attachment(img)
            elif img.attachment_entry.name == "Label":
                self._label = self.image_from_attachment(img)
            elif img.attachment_entry.name == "Macro":
                self._macro = self.image_from_attachment(img)

    def image_from_attachment(self, attachment) -> CZIAttachment:
        if attachment.attachment_entry.content_file_type != "CZI":
            image = CZIVIPSAttachment(attachment)
        else:
            image = CZINAtiveAttachment(attachment)
        return image

    @property
    def label_image(self):
        """
        Return the label image handler, if a label image has been identified
        """

        return self._label

    @property
    def macro_image(self):
        """
        Return the macro image handler, if a macro image has been identified
        """

        return self._macro

    @property
    def overview_image(self):
        """
        Return the overview image handler, if a overview image has been identified
        """
        return self._overview

    @property
    def thumbnail_image(self):
        """
        Return the thumbnail image handler, if a overview image has been identified
        """
        return self._thumbnail
