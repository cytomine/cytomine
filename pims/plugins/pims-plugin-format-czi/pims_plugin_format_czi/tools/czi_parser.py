from __future__ import annotations

import argparse
import czifile
from io import BytesIO
import numpy as np
from PIL import Image as PILImage
from pylibCZIrw import czi
from pyvips import Image as VIPSImage
from pyvips import BandFormat
from typing import Any, Optional


pixel_types_to_vips_band_type = {
    'Bgr24': BandFormat.UCHAR,
    'Rgb24': BandFormat.UCHAR,
    'Gray8': BandFormat.UCHAR,
    'Bgr48': BandFormat.USHORT,
    'Rgb48': BandFormat.USHORT,
    'Gray16': BandFormat.USHORT,
    }


pixel_types_to_numpy_dtype = {
    'Bgr24': np.uint8,
    'Rgb24': np.uint8,
    'Gray8': np.uint8,
    'Bgr48': np.uint16,
    'Rgb48': np.uint16,
    'Gray16': np.uint16,
    }


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


def pretty_print(title: Optional[str], data: Any, indent: int = 0) -> None:
    if data is None:
        return
    if isinstance(data, dict):
        if title is not None:
            print(' ' * indent + str(title) + ':')
        for key, value in data.items():
            pretty_print(key, value, indent + 2)
    elif isinstance(data, list):
        if title is not None:
            print(' ' * indent + str(title) + ':')
        for i, item in enumerate(data):
            print(' ' * (indent + 2), f"Item {i}:")
            pretty_print(None, item, indent + 4)
    else:
        if title is not None:
            print(' ' * indent + title + ": " + str(data))
        else:
            print(' ' * indent + str(data))


if __name__ == '__main__':
    args_parser = argparse.ArgumentParser()
    args_parser.add_argument("file", nargs=1, default=None)
    args_parser.add_argument("--metadata", action='store_true', default=False)
    args_parser.add_argument("--custom", action='store_true', default=False)
    args_parser.add_argument("--verbose", action='store_true', default=False)
    args_parser.add_argument("--attachment", default=None)
    args_parser.add_argument("--save", action='store_true', default=False)
    args_parser.add_argument("--raw", action='store_true', default=False)
    args_parser.add_argument("--roi", default=None)
    args_parser.add_argument("--zoom", default=1)
    args_parser.add_argument("--scene", default=None)
    args_parser.add_argument("--c", default=0)
    args_parser.add_argument("--vips", action='store_true', default=False)
    args_parser.add_argument("--format", default='tif')

    args = args_parser.parse_args()

    czi_reader = czi.CziReader(args.file[0])

    print("Dimensions:", czi_reader.total_bounding_box)
    if czi_reader.scenes_bounding_rectangle:
        print("Scenes:")
        for scene_index, scene in czi_reader.scenes_bounding_rectangle.items():
            print(f"  {scene_index}: {scene}")
    else:
        print("No scenes")
    print("Pixel types:")
    for channel, pixel_type in czi_reader.pixel_types.items():
        print(f"  {channel}: {pixel_type}")

    print("Attachments:")
    czi_file = czifile.CziFile(args.file[0])
    for a in czi_file.attachments():
        print(f"  {a.attachment_entry.name}: {a.attachment_entry.content_file_type}")
        if args.attachment is not None:
            for a in czi_file.attachments():
                if args.attachment == a.attachment_entry.name:
                    if args.raw or a.attachment_entry.content_file_type != "CZI":
                        if args.save:
                            print("Saving", a.attachment_entry.name, "as", a.attachment_entry.filename)
                            a.save()
                    else:
                        raw_data = a.data(raw=True)
                        image_data = BytesIO(raw_data)
                        czi_embedded = czifile.CziFile(image_data)
                        if args.verbose:
                            print(czi_embedded)
                        if args.save:
                            name = f"{a.attachment_entry.name}.{args.format}"
                            print("Saving", a.attachment_entry.name, "as", name)
                            data = czi_embedded.asarray()
                            data = data[args.c]
                            if args.vips:
                                image = VIPSImage.new_from_memory(
                                    data,
                                    data.shape[1], data.shape[0],
                                    data.shape[2],
                                    format=numpy_to_vips_band_type[data.dtype],
                                    )
                                image.write_to_file(name)
                            else:
                                if data.shape[2] == 3:
                                    mode = "RGB"
                                else:
                                    mode = "L"
                                image = PILImage.fromarray(data, mode)
                                image.save(name)
        elif args.save:
            if args.roi is not None:
                roi = tuple(int(v) for v in args.roi.split(','))
            else:
                roi = None
            data = czi_reader.read(scene=args.scene, roi=roi, zoom=float(args.zoom))
            if roi is not None:
                name = f"output-{args.roi.replace(',', '-')}-{args.zoom}.{args.format}"
            else:
                name = f"output-{args.zoom}.{args.format}"
            print("Saving", name)
            if args.vips:
                image = VIPSImage.new_from_memory(
                    np.ascontiguousarray(data),
                    data.shape[1], data.shape[0],
                    data.shape[2],
                    format=pixel_types_to_vips_band_type[czi_reader.pixel_types[args.c]],
                    )
                image.write_to_file(name)
            else:
                image = PILImage.fromarray(data.astype(pixel_types_to_numpy_dtype[czi_reader.pixel_types[args.c]]))
                image.save(name)

    if args.metadata:
        pretty_print("Metadata", czi_reader.metadata)

    if args.custom:
        pretty_print("Custom attributes", czi_reader.custom_attributes_metadata)
