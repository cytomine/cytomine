from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from pims.formats import AbstractFormat
    from pims.files.file import Path


class AbstractConvertor(ABC):
    """
    Base convertor. All convertors must extend this class.
    """
    def __init__(self, source: AbstractFormat):
        """
        Initializer.

        Parameters
        ----------
        source
            The image format to convert
        """
        self.source = source

    def convert(self, dest_path: Path) -> bool:
        """
        Convert the image in this format to another one at a given destination
        path.

        Returns
        -------
        result
            Whether the conversion succeeded or not
        """
        raise NotImplementedError()

    @abstractmethod
    def conversion_format(self) -> type[AbstractFormat]:
        """
        Get the format to which the image in this format will be converted,
        if needed.
        """
        raise NotImplementedError()
