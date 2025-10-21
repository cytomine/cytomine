import logging
from lxml import etree
from pathlib import Path
from typing import List

from cytomine.models import UploadedFile

from pims.config import get_settings
from pims.importer.importer import run_import
from pims.importer.listeners import CytomineListener
from pims.schemas.operations import ImportResult, ImportSummary

logger = logging.getLogger("pims.app")


FILE_ROOT_PATH = Path(get_settings().root)
WRITING_PATH = Path(get_settings().writing_path)


class ImageImporter:
    def __init__(self, base_path: Path, cytomine_auth, user, storage_id) -> None:
        self.base_path = base_path
        self.cytomine_auth = cytomine_auth
        self.user = user
        self.storage_id = storage_id

    def get_images(self) -> None:
        dataset_xml_path = self.base_path / "METADATA" / "dataset.xml"
        tree = etree.parse(dataset_xml_path)
        root = tree.getroot()

        images = root.findall(".//IMAGE_REF")
        return [image.get("alias") for image in images]

    def import_image(self, alias: str, projects: List[str]) -> ImportResult:
        image_path = self.base_path / "IMAGES" / alias
        if not image_path.exists():
            logger.warning(f"'{image_path}' does not exist!")
            return ImportResult(
                name=image_path.name,
                success=False,
                message="Image does not exist",
            )

        if is_already_imported(image_path, Path(FILE_ROOT_PATH)):
            logger.warning(f"'{image_path}' already imported!")
            return ImportResult(
                name=image_path.name,
                success=True,
                message="Already imported",
            )

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

            return ImportResult(name=image_path.name, success=True)
        except Exception as e:
            logger.error(f"Failed to import '{image_path.name}': {e}")
            return ImportResult(name=image_path.name, success=False, message=e)

    def run(self, projects=[]) -> ImportSummary:
        logger.info("[START] Import images...")
        results = [self.import_image(image, projects) for image in self.get_images()]
        successful = sum(1 for r in results if r.success)
        logger.info("[END] Import images...")

        return ImportSummary(
            total=len(results),
            successful=successful,
            failed=len(results) - successful,
            results=results,
        )


def is_already_imported(image_path: Path, data_path: Path) -> bool:
    """Check if an image was already imported."""

    for upload_dir in data_path.iterdir():
        if not upload_dir.is_dir():
            continue

        for candidate in upload_dir.iterdir():
            if candidate.is_symlink() and candidate.resolve() == image_path.resolve():
                return True

    return False
