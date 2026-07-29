# -*- coding: utf-8 -*-

from typing import Any

from cytomine.cytomine import Cytomine
from cytomine.models import (
    Storage,
    StorageCollection,
    UploadedFile,
    UploadedFileCollection,
)
from tests.conftest import random_string

class TestStorage:
    def test_storage(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        name = random_string()
        storage = Storage(name, dataset["user"].id).save()
        assert isinstance(storage, Storage)
        assert storage.name == name

        storage = Storage().fetch(storage.id)
        assert isinstance(storage, Storage)
        assert storage.name == name

        name = random_string()
        storage.name = name
        storage.update()
        assert isinstance(storage, Storage)
        assert storage.name == name

        # TODO: Storage delete does not work on Cytomine-Core
        # storage.delete()
        # assert(not Storage().fetch(storage.id))

    def test_storages(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        storages = StorageCollection().fetch()
        assert isinstance(storages, StorageCollection)

class TestUploadedFile:
    def test_uploaded_file(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        storages = StorageCollection().fetch()
        filename = "filename"
        uf = UploadedFile(
            "original",
            filename,
            id_user=connect.current_user.id,  # type: ignore
            size=1,
            ext="ext",
            contentType="contentType",
            id_storage=storages[0].id,  # type: ignore
        ).save()
        assert isinstance(uf, UploadedFile)
        assert uf.filename == filename

        filename = filename + "bis"
        uf.filename = filename
        uf.update()
        assert isinstance(uf, UploadedFile)
        assert uf.filename == filename

        uf.delete()
        assert not UploadedFile().fetch(uf.id)

    def test_uploaded_files(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        uploaded_files = UploadedFileCollection().fetch()
        assert isinstance(uploaded_files, UploadedFileCollection)
