import logging
import os
from collections import defaultdict
from lxml import etree
from typing import Optional

from cytomine import Cytomine
from cytomine.models import (
    ImageInstanceCollection,
    OntologyCollection,
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
from pims.importer.annotation import AnnotationImporter
from pims.importer.importer import run_import
from pims.importer.ontology import OntologyImporter
from pims.importer.image import ImageImporter
from pims.importer.listeners import CytomineListener
from pims.importer.metadata import MetadataValidator
from pims.importer.ontology import OntologyImporter
from pims.schemas.auth import ApiCredentials, CytomineAuth
from pims.schemas.operations import ImportResponse, ImportResult

logger = logging.getLogger("pims.app")

DATASET_ROOT = Path(get_settings().dataset_path)
WRITING_PATH = Path(get_settings().writing_path)
FILE_ROOT_PATH = Path(get_settings().root)


class BucketParser:
    def __init__(self, root: Path):
        self.root = root  # Root path of the dataset
        self.datasets = {}  # Path to main dataset and complementary datasets
        # Relation between the main dataset and complementary dataset, parent -> [children]
        self.dependency = defaultdict(list)

    @property
    def parent(self):
        return next(iter(self.dependency.keys()))

    @property
    def children(self):
        return next(iter(self.dependency.values()))

    def discover(self):
        for child in self.root.iterdir():
            if not child.is_dir():
                logger.warning(f"'{child}' is not a folder!")
                logger.warning(f"Skipping '{child}' ...")
                continue

            metadata_dataset_path = child / "METADATA" / "dataset.xml"
            if not metadata_dataset_path.exists():
                logger.warning(f"'{metadata_dataset_path}' does not exist!")
                logger.warning(f"Skipping '{child}' ...")
                continue

            tree = etree.parse(metadata_dataset_path)
            root = tree.getroot()

            dataset = root.find(".//DATASET")
            dataset_name = dataset.get("alias")
            self.datasets[dataset_name] = child

            complement = root.find(".//COMPLEMENTS_DATASET_REF")
            if complement is not None:
                self.dependency[complement.get("alias")].append(dataset_name)


class DatasetImporter:
    def __init__(self, storage_id: str, dataset_names: str) -> None:
        self.storage_id = storage_id
        self.dataset_names = dataset_names

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

    def import_metadata(self, root) -> None:
        dataset_path = [d for d in Path(root).iterdir() if d.is_dir()].pop()
        dataset_name = os.path.basename(root)
        metadata_path = dataset_path / "METADATA"
        if not metadata_path.exists():
            logger.info("Metadata folder not found, no validation will be performed")
            return

        validator = MetadataValidator()
        valid = validator.validate(metadata_path)
        if not valid:
            logger.error(f"'{dataset_name}' Metadata failed to validate.")
        else:
            logger.info(f"'{dataset_name}' Metadata validated successfully.")

    def import_ontology(self, root_path: Path) -> None:
        importer = OntologyImporter(root_path)
        ontology = importer.load()
        print(f"ONTOLOGY {ontology}")

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
        datasets = [os.path.join(DATASET_ROOT, dataset) for dataset in dataset_paths]

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

                self.import_metadata(dataset_root)
                self.import_ontology(Path(dataset_root))

        return ImportResponse(
            valid_datasets=valid_datasets,
            invalid_datasets=[],
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


def get_project(key: str, projects: dict[str, Project]) -> Project:
    if key in projects:
        return projects[key]

    project = Project(name=key).save()
    return project


def run_import_datasets(
    cytomine_auth: CytomineAuth,
    credentials: ApiCredentials,
    storage_id: str,
) -> None:
    buckets = (Path(entry.path) for entry in os.scandir(DATASET_ROOT) if entry.is_dir())

    with Cytomine(**cytomine_auth.model_dump(), configure_logging=False) as c:
        if not c.current_user:
            raise AuthenticationException("PIMS authentication to Cytomine failed.")

        cyto_keys = c.get(f"userkey/{credentials.public_key}/keys.json")
        private_key = cyto_keys["privateKey"]

        if sign_token(private_key, credentials.token) != credentials.signature:
            raise AuthenticationException("Authentication to Cytomine failed")

        c.set_credentials(credentials.public_key, private_key)

        storage = Storage().fetch(storage_id)
        if not storage:
            raise CytomineProblem(f"Storage {storage_id} not found")

        project_collection = ProjectCollection().fetch()
        projects = {project.name: project for project in project_collection}

        for bucket in buckets:
            parser = BucketParser(bucket)
            parser.discover()

            project = get_project(parser.parent, projects)

            ImageImporter(
                bucket / parser.parent,
                cytomine_auth,
                c.current_user,
                storage_id,
            ).run(projects=[project])

            images = ImageInstanceCollection().fetch_with_filter("project", project.id)
            ontologies = OntologyCollection().fetch()

            for child in parser.children:
                child_path = bucket / child
                ontology = OntologyImporter(child_path).run()
                ontologies.append(ontology)

                if project.ontology is None:
                    project.ontology = ontology.id
                    project.update()

                AnnotationImporter(child_path, images, ontologies).run()
