import logging
import os
from typing import Optional

from cytomine import Cytomine
from cytomine.models import (
    Project,
    ProjectCollection,
    Storage,
    UploadedFile,
    User,
)

from pims.api.exceptions import AuthenticationException, CytomineProblem
from pims.api.utils.cytomine_auth import sign_token
from pims.config import get_settings
from pims.files.file import Path
from pims.importer.importer import run_import
from pims.importer.listeners import CytomineListener
from pims.schemas.auth import ApiCredentials, CytomineAuth
from pims.schemas.operations import ImportResponse, ImportResult

logger = logging.getLogger("pims.app")

DATASET_ROOT = Path(get_settings().dataset_path)
WRITING_PATH = Path(get_settings().writing_path)
FILE_ROOT_PATH = Path(get_settings().root)


class DatasetImporter:
    def __init__(self, storage_id: str, dataset_names: str) -> None:
        self.storage_id = storage_id
        self.dataset_names = dataset_names

    def _check_dataset_structure(self, root: str) -> None:
        dataset_directory = [entry for entry in os.scandir(root) if entry.is_dir()]
        if len(dataset_directory) != 1:
            raise ValueError(f"Expected 1 directory, found {len(dataset_directory)}")

        dataset_path = dataset_directory.pop()
        has_images = any(
            entry.name.upper() == "IMAGES" and entry.is_dir()
            for entry in os.scandir(dataset_path)
        )

        if not has_images:
            raise ValueError(f"IMAGES directory not found in {dataset_path.path}")

    def _is_valid_dataset(self, dataset_path: str) -> bool:
        try:
            if not os.path.isdir(dataset_path):
                return False
            self._check_dataset_structure(dataset_path)
            return True
        except ValueError:
            return False

    def filter_dataset(self, dataset_paths) -> tuple[list[str], list[str]]:
        valid_datasets = []
        invalid_datasets = []

        for dataset in dataset_paths:
            dataset_path = os.path.join(DATASET_ROOT, dataset)

            if self._is_valid_dataset(dataset_path):
                valid_datasets.append(dataset_path)
            else:
                invalid_datasets.append(dataset)

        return valid_datasets, invalid_datasets

    def import_images(
        self,
        root: Path,
        create_project: Optional[bool],
        project_names: list[str],
        user: User,
        cytomine_auth: CytomineAuth,
    ) -> ImportResult:
        result = ImportResult(
            uploaded_files=[],
            failed_files=[],
            skipped_files=[],
        )
        dataset_path = [d for d in Path(root).iterdir() if d.is_dir()].pop()
        dataset_name = os.path.basename(root)

        if create_project:
            logger.info(f"Create or get project '{dataset_name}'")
            if dataset_name in project_names:
                project = project_names[dataset_name]
                result.project_created = False
            else:
                project = Project(name=dataset_name).save()
                result.project_created = True

        image_directory = Path(dataset_path) / "IMAGES"
        if not image_directory.exists():
            image_directory = Path(dataset_path) / "images"
        image_paths = list(image_directory.iterdir())

        for image_path in image_paths:
            if is_already_imported(image_path, Path(FILE_ROOT_PATH)):
                logger.debug(f"'{image_path}' already imported!")
                result.skipped_files.append(image_path.name)
                continue

            tmp_path = Path(WRITING_PATH, image_path.name)
            tmp_path.symlink_to(image_path, target_is_directory=image_path.is_dir())

            uploadedFile = UploadedFile(
                original_filename=image_path.name,
                filename=str(tmp_path),
                size=image_path.size,
                ext="",
                content_type="",
                id_projects=[project.id] if create_project else [],
                id_storage=self.storage_id,
                id_user=user.id,
                status=UploadedFile.UPLOADED,
            )

            projects = ProjectCollection()
            projects.append(project)
            cytomine_listener = CytomineListener(
                cytomine_auth,
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
                result.uploaded_files.append(image_path.name)
            except Exception as e:
                result.failed_files.append(
                    {
                        "file": image_path.name,
                        "error": str(e),
                    }
                )

        return result

    def import_metadata() -> None:
        # Validate metadata
        ...

    def import_datasets(
        self,
        cytomine_auth: CytomineAuth,
        credentials: ApiCredentials,
        create_project: Optional[bool] = None,
    ) -> ImportResponse:
        WRITING_PATH.mkdir(parents=True, exist_ok=True)

        dataset_paths = (
            self.dataset_names.split(",")
            if self.dataset_names
            else os.listdir(DATASET_ROOT)
        )
        datasets, invalid_datasets = self.filter_dataset(dataset_paths)

        valid_datasets = {}

        with Cytomine(**cytomine_auth.model_dump(), configure_logging=False) as c:
            if not c.current_user:
                raise AuthenticationException("PIMS authentication to Cytomine failed.")

            cyto_keys = c.get(f"userkey/{credentials.public_key}/keys.json")
            private_key = cyto_keys["privateKey"]

            if sign_token(private_key, credentials.token) != credentials.signature:
                raise AuthenticationException("Authentication to Cytomine failed")

            c.set_credentials(credentials.public_key, private_key)

            storage = Storage().fetch(self.storage_id)
            if not storage:
                raise CytomineProblem(f"Storage {self.storage_id} not found")

            current_projects = ProjectCollection().fetch()
            project_names = {project.name: project for project in current_projects}

            for dataset_root in datasets:
                dataset_name = os.path.basename(dataset_root)
                valid_datasets[dataset_name] = self.import_images(
                    dataset_root,
                    create_project,
                    project_names,
                    c.current_user,
                    cytomine_auth,
                )

        return ImportResponse(
            valid_datasets=valid_datasets,
            invalid_datasets=invalid_datasets,
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
