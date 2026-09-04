# -*- coding: utf-8 -*-

from typing import Any

from cytomine.models.collection import Collection
from cytomine.models.model import Model

class Storage(Model):
    def __init__(
        self,
        name: str | None = None,
        id_user: int | None = None,
        **attributes: Any,
    ) -> None:
        super().__init__()
        self.name = name
        self.userId = id_user
        self.populate(attributes)

class StorageCollection(Collection):
    def __init__(
        self,
        filters: dict[str, Any] | None = None,
        max: int = 0,
        offset: int = 0,
        **parameters: Any,
    ) -> None:
        super().__init__(Storage, filters, max, offset)
        self._allowed_filters = [None]

        self.all = None
        self.set_parameters(parameters)

class UploadedFile(Model):
    # Old codes
    UPLOADED = 0
    CONVERTED = 1
    DEPLOYED = 2
    ERROR_FORMAT = 3
    ERROR_CONVERT = 4
    UNCOMPRESSED = 5
    TO_DEPLOY = 6
    # --

    DETECTING_FORMAT = 10
    ERROR_FORMAT = 11
    EXTRACTING_DATA = 20
    ERROR_EXTRACTION = 21
    CONVERTING = 30
    ERROR_CONVERSION = 31
    DEPLOYING = 40
    ERROR_DEPLOYMENT = 41
    DEPLOYED = 100
    EXTRACTED = 102
    CONVERTED = 104

    def __init__(
        self,
        original_filename: str | None = None,
        filename: str | None = None,
        size: int | None = None,
        ext: str | None = None,
        content_type: str | None = None,
        id_projects: list[int] | None = None,
        id_storage: int | None = None,
        id_user: int | None = None,
        status: int | None = None,
        id_parent: int | None = None,
        **attributes: Any,
    ) -> None:
        super().__init__()
        self.originalFilename = original_filename
        self.filename = filename
        self.path = None
        self.size = size
        self.ext = ext
        self.contentType = content_type
        self.projects = id_projects
        self.storage = id_storage
        self.user = id_user
        self.status = status
        self.parent = id_parent
        self.populate(attributes)

    def __str__(self) -> str:
        return f"[{self.callback_identifier}] {self.id} : {self.filename}"

class UploadedFileCollection(Collection):
    def __init__(
        self,
        filters: dict[str, Any] | None = None,
        max: int = 0,
        offset: int = 0,
        **parameters: Any,
    ) -> None:
        super().__init__(UploadedFile, filters, max, offset)
        self._allowed_filters = [None]

        self.all = None
        self.parent = None
        self.set_parameters(parameters)
