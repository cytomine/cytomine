from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from pims.formats.utils.abstract import CachedDataPath


class AbstractChecker(ABC):
    """
    Base checker. All format checkers must extend this class.
    """

    @classmethod
    @abstractmethod
    def match(cls, pathlike: CachedDataPath) -> bool:
        """Whether the path is in this format or not."""
        pass


class SignatureChecker(AbstractChecker, ABC):
    """
    Base signature checker. Add helper to get file signature.
    """
    @classmethod
    def get_signature(cls, pathlike: CachedDataPath) -> bytearray:
        """Get cached file signature"""
        return pathlike.get_cached('signature', pathlike.path.signature)
