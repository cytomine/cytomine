from __future__ import annotations

import argparse
from pylibCZIrw import czi
from typing import Any, Optional


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
    args_parser.add_argument("--verbose", action='store_true', default=False)

    args = args_parser.parse_args()

    czi_file = czi.CziReader(args.file[0])

    print("Dimensions:", czi_file.total_bounding_box)

    if args.verbose:
        pretty_print("Metadata", czi_file.metadata)

        pretty_print("Custom attributes", czi_file.custom_attributes_metadata)
