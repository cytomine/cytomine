import logging
import os

from cytomine import Cytomine
from cytomine.models import (
    Project,
    ProjectCollection,
    Storage,
    UploadedFile,
)

from pims.api.exceptions import AuthenticationException, CytomineProblem
from pims.api.operations import INTERNAL_URL_CORE
from pims.api.utils.cytomine_auth import parse_authorization_header, parse_request_token, sign_token
from pims.config import get_settings
from pims.files.file import Path
from pims.importer.importer import run_import
from pims.importer.listeners import CytomineListener
from pims.importer.utils import check_dataset_structure, is_already_imported

log = logging.getLogger("pims.app")

DATASET_PATH = get_settings().dataset_path
PENDING_PATH = Path(get_settings().pending_path)
WRITING_PATH = Path(get_settings().writing_path)
FILE_ROOT_PATH = Path(get_settings().root)


class DatasetImporter:
    def import_dataset(dataset_names, create_project, config):
        Path(WRITING_PATH).mkdir(parents=True, exist_ok=True)

        # Dataset discovery
        valid_datasets = []
        invalid_datasets = {}

        dataset_paths = (
            dataset_names.split(",") if dataset_names else os.listdir(DATASET_PATH)
        )
        for dataset in dataset_paths:
            dataset_path = os.path.join(DATASET_PATH, dataset)

            if not os.path.isdir(dataset_path):
                continue

            is_valid, missing = check_dataset_structure(dataset_path)

            if is_valid:
                valid_datasets.append(dataset_path)
            else:
                invalid_datasets[dataset_path] = missing

        public_key, signature = parse_authorization_header(request.headers)
        cytomine_auth = (
            INTERNAL_URL_CORE,
            config.cytomine_public_key,
            config.cytomine_private_key,
        )

        response = {
            "valid_datasets": {
                os.path.basename(dataset_path): {
                    "uploaded_files": [],
                    "failed_files": [],
                    "skipped_files": [],
                }
                for dataset_path in valid_datasets
            },
            "invalid_datasets": invalid_datasets,
        }

        with Cytomine(*cytomine_auth, configure_logging=False) as c:
            if not c.current_user:
                raise AuthenticationException("PIMS authentication to Cytomine failed.")

            cyto_keys = c.get(f"userkey/{public_key}/keys.json")
            private_key = cyto_keys["privateKey"]

            if sign_token(private_key, parse_request_token(request)) != signature:
                raise AuthenticationException("Authentication to Cytomine failed")

            c.set_credentials(public_key, private_key)
            user = c.current_user

            storage = Storage().fetch(storage_id)
            if not storage:
                raise CytomineProblem(f"Storage {storage_id} not found")

            # Filter out existing datasets
            current_projects = ProjectCollection().fetch()
            project_names = {project.name: project for project in current_projects}

            for dataset_root in valid_datasets:
                dataset_path = [
                    d for d in Path(dataset_root).iterdir() if d.is_dir()
                ].pop()
                dataset_name = os.path.basename(dataset_root)

                if create_project:
                    if dataset_name in project_names:
                        project = project_names[dataset_name]
                        response["valid_datasets"][dataset_name][
                            "project_created"
                        ] = False
                    else:
                        project = Project(name=dataset_name).save()
                        response["valid_datasets"][dataset_name][
                            "project_created"
                        ] = True

                image_directory = Path(dataset_path) / "IMAGES"
                if not image_directory.exists():
                    image_directory = Path(dataset_path) / "images"
                image_paths = list(image_directory.iterdir())

                for image_path in image_paths:
                    if is_already_imported(image_path, Path(FILE_ROOT_PATH)):
                        response["valid_datasets"][dataset_name][
                            "skipped_files"
                        ].append(image_path.name)
                        continue

                    tmp_path = Path(WRITING_PATH, image_path.name)
                    tmp_path.symlink_to(
                        image_path, target_is_directory=image_path.is_dir()
                    )

                    uploadedFile = UploadedFile(
                        original_filename=image_path.name,
                        filename=str(tmp_path),
                        size=image_path.size,
                        ext="",
                        content_type="",
                        id_projects=[project.id] if create_project else [],
                        id_storage=storage_id,
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
                        response["valid_datasets"][dataset_name][
                            "uploaded_files"
                        ].append(image_path.name)
                    except Exception as e:
                        response["valid_datasets"][dataset_name]["failed_files"].append(
                            {
                                "file": image_path.name,
                                "error": str(e),
                            }
                        )
