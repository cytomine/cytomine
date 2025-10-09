import czifile
from io import BytesIO
import logging
from math import ceil, log2
import numpy as np
from pims.formats.utils.structures.pyramid import Pyramid
from pims.utils import UNIT_REGISTRY
from pylibCZIrw import czi
from pyvips import Image as VIPSImage
from pyvips import BandFormat

from typing import Any, Iterable, Optional


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


def has_deep_attr(data: dict, attrs: Iterable[str]):
    curr_data = data
    for attr_name in attrs:
        if not hasattr(curr_data, attr_name):
            return False
        curr_data = getattr(curr_data, attr_name)
    return True


def get_deep_attr(data: Any, attrs: Iterable[str], default: Any=None):
    if not has_deep_attr(data, attrs):
        return default
    curr_data = data
    for attr_name in attrs:
        curr_data = getattr(curr_data, attr_name)
    return curr_data


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
    """
    Base class to describe a CZI attachment (Not only images are supported).
    """

    def __init__(self, attachment):
        self.attachment = attachment

    @property
    def width(self) -> int:
        """
        Returns the width of the image
        """

        raise NotImplementedError()

    @property
    def height(self) -> int:
        """
        Returns the height of the image
        """

        raise NotImplementedError()

    @property
    def n_components(self) -> int:
        """
        Returns the number of components per pixels
        """

        raise NotImplementedError()

    def read(self) -> VIPSImage:
        """
        Read the whole image and returns it as VIPS image instance
        """

        raise NotImplementedError()


class CZIVIPSAttachment(CZIAttachment):
    """
    Class that contains a simple attachment image, (e.g. a JPEG or PNG image)
    This class will use a VIPS image to manage the attachment.
    """

    def __init__(self, attachment):
        super().__init__(attachment)
        raw_data = attachment.data(raw=True)
        self._image = VIPSImage.new_from_buffer(raw_data, options="")

    @property
    def width(self) -> int:
        return self._image.width

    @property
    def height(self) -> int:
        return self._image.height

    @property
    def n_components(self) -> int:
        return self._image.bands

    def read(self) -> VIPSImage:
        return self._image


class CZINAtiveAttachment(CZIAttachment):
    """
    Class that contains an attachment image stored as an embedded CZI image.
    This class will use a czifile image to manage the attachment as ibpyczirw
    does not support in memory images.
    """

    def __init__(self, attachment):
        super().__init__(attachment)
        raw_data = attachment.data(raw=True)
        image_data = BytesIO(raw_data)
        self._czi = czifile.CziFile(image_data)

    @property
    def width(self) -> int:
        return self._czi.shape[2]

    @property
    def height(self) -> int:
        return self._czi.shape[1]

    @property
    def n_components(self) -> int:
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


class CZIFile():
    """
    Main class to provide high level management of CZI images
    """

    # Map the pixel format of image into Numpy types
    PixelFormatToNPType = {
        "Gray8": np.uint8,
        "Gray16": np.uint16,
        "Gray32Float": np.float32,
        "Bgr24": np.uint8,
        "Bgr48": np.uint16,
        "Bgr96Float": np.float32,
        }

    # Map the number of components per pixels
    PixelFormatToNumComponents = {
        "Gray8": 1,
        "Gray16": 1,
        "Gray32Float": 1,
        "Bgr24": 3,
        "Bgr48": 3,
        "Bgr96Float": 3,
        }

    # Map the pixel format of image into PIL format
    PixelFormatToPIL = {
        "Gray8": "L",
        "Gray16": "L",
        "Gray32Float": "L",
        "Bgr24": "RGB",
        "Bgr48": "RGB",
        "Bgr96Float": "RGB",
        }

    # Map the pixel format of image into VIPS format
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

        # We have two embedded CZI file handlers, one for the main image, based on libpyczirw
        # And another one based on czi file, to manage attachments as libpyczirw does not
        # expose an API for them
        self._czi_file_reader = czi.CziReader(path)
        self._attachment_reader = czifile.CziFile(path)

        self._thumbnail = None
        self._overview = None
        self._label = None
        self._macro = None

        self._pixel_size_x = None
        self._pixel_size_y = None
        self._depth = 1
        self._start_depth = 0
        self._depth_scale = None
        self._depth_unit = None
        self._duration = 1
        self._start_duration = 1
        self._duration_scale = None
        self._duration_unit = None
        self._frame_rate = None
        self._frame_rate_unit = None
        self._n_concrete_channels = 1
        self._start_concrete_channels = 0
        self._num_components = None
        self._component_type = None

        # Calculate the dimensions of the main image, using the bounding box of the CZI file
        # The bounding box contains the limit of the image in all the dimension, not only =X and Y
        bounding_box = self._czi_file_reader.total_bounding_box
        self._width = bounding_box['X'][1] - bounding_box['X'][0]
        self._height = bounding_box['Y'][1] - bounding_box['Y'][0]

        # Check the existence and number of extra dimensions and channels.
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

        metadata = self._metadata_dict_obj.ImageDocument.Metadata

        # Extract the image information from the metadata
        # As the CZI metadata are not normalized, we have to check several metadata names
        # to find the actual values.
        if hasattr(metadata, 'ImageScaling'):
            physical_pixel_size = metadata.ImageScaling.ImagePixelSize
            self._pixel_size_x = float(physical_pixel_size.split(',')[0])
            self._pixel_size_y = float(physical_pixel_size.split(',')[1])
        elif hasattr(metadata, 'Scaling'):
            if hasattr(metadata.Scaling, 'AutoScaling'):
                physical_pixel_size = metadata.Scaling.AutoScaling.CameraPixelDistance
                self._pixel_size_x = float(physical_pixel_size.split(',')[0])
                self._pixel_size_y = float(physical_pixel_size.split(',')[1])
            elif hasattr(metadata.Scaling, 'Items') and hasattr(metadata.Scaling.Items, 'Pixel'):
                for pixel in metadata.Scaling.Items.Pixel:
                    if getattr(pixel, '@Id') == 'X':
                        self._pixel_size_x = float(pixel.Value)
                    elif getattr(pixel, '@Id') == 'Y':
                        self._pixel_size_y = float(pixel.Value)
            else:
                self._pixel_size_x = 0
                self._pixel_size_y = 0
        else:
            self._pixel_size_x = 0
            self._pixel_size_y = 0

        if hasattr(metadata.Information, 'Instrument'):
            calibrated_magnification = 0
            nominal_magnification = 0
            if hasattr(metadata.Information.Instrument, 'Objectives'):
                objective = metadata.Information.Instrument.Objectives.Objective
                if hasattr(objective, 'CalibratedMagnification'):
                    calibrated_magnification = float(objective.CalibratedMagnification)
                if hasattr(metadata.Information.Instrument.Objectives.Objective, 'NominalMagnification'):
                    nominal_magnification = float(objective.NominalMagnification)
                self._calibrated_magnification = max(calibrated_magnification, nominal_magnification)
        else:
            self._calibrated_magnification = 0

        if hasattr(metadata.Information, 'Image'):
            if hasattr(metadata.Information.Image, 'Dimensions'):
                dimensions = metadata.Information.Image.Dimensions
                self._channel_names = [None for _ in range(self._n_concrete_channels)]
                self._emission_wavelengths = [None for _ in range(self._n_concrete_channels)]
                self._excitation_wavelengths = [None for _ in range(self._n_concrete_channels)]
                if hasattr(dimensions, 'Channels'):
                    channels = dimensions.Channels.Channel
                    if channels is None:
                        channels = []
                    elif not isinstance(channels, list):
                        channels = [channels]
                    for index, channel in enumerate(channels):
                        if hasattr(channel, '@Name'):
                            name = getattr(channel, '@Name')
                        elif hasattr(channel, 'Fluor'):
                            name = getattr(channel, 'Fluor')
                        elif hasattr(channel, '@Id'):
                            name = getattr(channel, '@id')
                        else:
                            name = None
                        self._channel_names[index] = name
                        if hasattr(channel, 'ExcitationWavelength'):
                            excitation_wavelength = getattr(channel, 'ExcitationWavelength')
                            if excitation_wavelength is not None:
                                if isinstance(excitation_wavelength, str):
                                    excitation_wavelength = float(excitation_wavelength) * UNIT_REGISTRY.Quantity('nm')
                                    self._excitation_wavelengths[index] = excitation_wavelength
                        if hasattr(channel, 'EmissionWavelength'):
                            emission_wavelength = getattr(channel, 'EmissionWavelength')
                            if emission_wavelength is not None:
                                if isinstance(excitation_wavelength, str):
                                    emission_wavelength = float(emission_wavelength) * UNIT_REGISTRY.Quantity('nm')
                                    self._emission_wavelengths[index] = emission_wavelength
                if hasattr(dimensions, 'T') and hasattr(dimensions.T, 'Positions'):
                    positions = dimensions.T.Positions
                    if hasattr(positions, 'Interval') and hasattr(positions.Interval, 'Increment'):
                        self._duration_scale = float(positions.Interval.Increment)
                        self._duration_unit = UNIT_REGISTRY.Quantity('second')
                if hasattr(dimensions, 'Z') and hasattr(dimensions.Z, 'Positions'):
                    positions = dimensions.Z.Positions
                    if hasattr(positions, 'Interval') and hasattr(positions.Interval, 'Increment'):
                        self._depth_scale = float(positions.Interval.Increment)
                        self._depth_unit = UNIT_REGISTRY.Quantity('micrometer')
                else:
                    self._channel_names = [None for _ in range(self._n_concrete_channels)]
            if hasattr(metadata.Information.Image, 'AcquisitionDateAndTime'):
                self._acquisition_datetime = metadata.Information.Image.AcquisitionDateAndTime
            else:
                self._acquisition_datetime = 0
        else:
            self._acquisition_datetime = 0

        if hasattr(metadata.Information, 'Instrument'):
            if hasattr(metadata.Information.Instrument, 'Microscopes'):
                if hasattr(metadata.Information.Instrument.Microscopes.Microscope, '@Name'):
                    self._device_model = getattr(metadata.Information.Instrument.Microscopes.Microscope, '@Name')
                elif hasattr(metadata.Information.Instrument.Microscopes.Microscope, 'System'):
                    self._device_model = getattr(metadata.Information.Instrument.Microscopes.Microscope, 'System')
            elif hasattr(metadata.Information.Instrument, 'Objectives'):
                if hasattr(metadata.Information.Instrument.Objectives.Objective, '@Name'):
                    self._device_model = getattr(metadata.Information.Instrument.Objectives.Objective, '@Name')
            else:
                self._device_model = "Zeiss Microscope"
        else:
            self._device_model = "Zeiss Microscope"

        # extract single gamma leyer
        gamma_attribute = ('DisplaySetting', 'Channels', 'Channel', 'Gamma')
        if has_deep_attr(metadata, gamma_attribute):
            self._gamma = float(get_deep_attr(metadata, gamma_attribute))
        else:
            self._gamma = None

        # Check the existance of associated images, like thumbnail, macro, ...
        self._analyze_images()

        # Build the pyramid object using a default tile size of 256x256
        # The library does not expose the configuration of the mosaic or subblocks.
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

    def channel_excitation_wavelength(self, index: int) -> Optional[float]:
        """
        Return the excitation wavelength of the channel, if known
        """

        if index < self._n_concrete_channels:
            return self._excitation_wavelengths[index]
        else:
            return None

    def channel_emission_wavelength(self, index: int) -> Optional[float]:
        """
        Return the emission wavelength of the channel, if known
        """

        if index < self._n_concrete_channels:
            return self._emission_wavelengths[index]
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
    def depth_scale(self) -> Optional[float]:
        """
        Return the physical scale of the depth (z) dimension
        """

        scale = self._depth_scale
        if self._depth_unit is not None:
            scale *= self._depth_unit
        return scale

    @property
    def duration(self) -> float:
        """
        Return the Image acquisition duration
        """

        return self._duration

    @property
    def duration_scale(self) -> Optional[float]:
        """
        Return the physical scale of the time dimension
        """

        scale = self._duration_scale
        if self._duration_unit is not None:
            scale *= self._duration_unit
        return scale

    @property
    def frame_rate(self) -> Optional[float]:
        """
        Return the frame rate of the time dimension
        """

        if self._duration_scale is not None and self._duration_scale > 0:
            duration = self._duration_scale
            if self._duration_unit is not None:
                duration = self._duration_unit
            frame_rate = 1 / duration
        else:
            frame_rate = None
        return frame_rate

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

    def _create_pyramid(self) -> Pyramid:
        """
        Create a normalized pyramid covering the whole image
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

        # When reading a CZI image, the area must always be in the global coordinates
        # the zoom factor is only to scale down the image, not applied on the coordinates
        scale = 2 ** level
        box = self._czi_file_reader.total_bounding_box
        # Calculate the area to read in the high resolution coordinates
        x_pos = x * scale + box['X'][0]
        y_pos = y * scale + box['Y'][0]
        scaled_width = width * scale
        scaled_height = height * scale
        # Crop the ROI if we are outside of the composited image
        if x_pos + scaled_width > box['X'][1]:
            scaled_width = box['X'][1] - x_pos
            width = int(scaled_width / scale)
        if y_pos + scaled_height > box['Y'][1]:
            scaled_height = box['Y'][1] - y_pos
            height = int(scaled_height / scale)
        roi = (x_pos, y_pos, scaled_width, scaled_height)

        # Calculate the zoom factor from the level
        zoom = (2 ** (self.pyramid.n_levels - level)) / (2 ** self.pyramid.n_levels)

        # Add the extra dimension coordinates
        plane = {"C": self._start_channels + c, "Z": self._start_depth + z, "T": self._start_duration + t}
        data = self._czi_file_reader.read(roi=roi, zoom=zoom, plane=plane, background_pixel=(1, 1, 1))
        # convert the raw image into a VIPS image
        # as the data array is not a continuous area in memory, we must convert it before
        # sending it to VIPS
        image = VIPSImage.new_from_memory(
            np.ascontiguousarray(data),
            width,
            height,
            self.num_components,
            format=self.numpy_to_vips_band_type[self.pixel_type]
            )
        if self._gamma is not None:
            original_format = image.format
            image = image.gamma(exponent=1/self._gamma).cast(original_format)
        return image

    def _analyze_images(self) -> None:
        """
        Detect the attachment and create the attachment instance associated
        """

        for img in self._attachment_reader.attachments():
            # The type of the attachment can only be guessed using the attachment name
            if img.attachment_entry.name == "Thumbnail":
                self._thumbnail = self.image_from_attachment(img)
            elif img.attachment_entry.name == "SlideOverview":
                self._overview = self.image_from_attachment(img)
            elif img.attachment_entry.name == "Label":
                self._label = self.image_from_attachment(img)
            elif img.attachment_entry.name == "Macro":
                self._macro = self.image_from_attachment(img)

    def image_from_attachment(self, attachment) -> CZIAttachment:
        """
        Create an attachment instance object based on the attachment type
        """

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
