import logging
from lxml import etree
from pathlib import Path
from typing import List

from cytomine.models import UploadedFile

from pims.config import get_settings
from pims.importer.importer import run_import
from pims.importer.listeners import CytomineListener

logger = logging.getLogger("pims.app")


FILE_ROOT_PATH = Path(get_settings().root)
WRITING_PATH = Path(get_settings().writing_path)


class ImageImporter:
    def __init__(self, base_path: Path, cytomine_auth, user, storage_id) -> None:
        self.base_path = base_path
        self.cytomine_auth = cytomine_auth
        self.user = user
        self.storage_id = storage_id

    def images(self):
        dataset_xml_path = self.base_path / "METADATA" / "dataset.xml"
        tree = etree.parse(dataset_xml_path)
        root = tree.getroot()

        images = root.findall(".//IMAGE_REF")
        images = [image.get("alias") for image in images]
        return images

    def import_image(self, alias: str, projects: List[str]):
        image_path = self.base_path / "IMAGES" / alias
        if is_already_imported(image_path, Path(FILE_ROOT_PATH)):
            logger.info(f"'{image_path}' already imported!")
            return

        tmp_path = Path(WRITING_PATH, image_path.name)
        tmp_path.symlink_to(image_path, target_is_directory=image_path.is_dir())

        uploadedFile = UploadedFile(
            original_filename=image_path.name,
            filename=str(tmp_path),
            size=image_path.size,
            ext="",
            content_type="",
            id_projects=[],
            id_storage=self.storage_id,
            id_user=self.user.id,
            status=UploadedFile.UPLOADED,
        )

        cytomine_listener = CytomineListener(
            self.cytomine_auth,
            uploadedFile,
            projects=projects,
            user_properties=iter([]),
        )

        try:
            run_import(
                tmp_path,
                image_path.name,
                extra_listeners=[cytomine_listener],
            )
        except Exception as e:
            pass

    def run(self, projects=[]) -> None:
        logger.info("Import images...")
        for image in self.images():
            self.import_image(image, projects)


def is_already_imported(image_path: Path, data_path: Path) -> bool:
    """Check if an image was already imported."""

    for upload_dir in data_path.iterdir():
        if not upload_dir.is_dir():
            continue

        for candidate in upload_dir.iterdir():
            if candidate.is_symlink() and candidate.resolve() == image_path.resolve():
                return True

    return False
