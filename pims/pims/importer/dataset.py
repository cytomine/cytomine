import logging
import os
from collections import defaultdict
from lxml import etree

from cytomine import Cytomine
from cytomine.models import (
    ImageInstanceCollection,
    OntologyCollection,
    Project,
    ProjectCollection,
    Storage,
)

from pims.api.exceptions import AuthenticationException, CytomineProblem
from pims.api.utils.cytomine_auth import sign_token
from pims.config import get_settings
from pims.files.file import Path
from pims.importer.annotation import AnnotationImporter
from pims.importer.ontology import OntologyImporter
from pims.importer.image import ImageImporter
from pims.importer.metadata import MetadataValidator
from pims.schemas.auth import ApiCredentials, CytomineAuth

logger = logging.getLogger("pims.app")

DATASET_ROOT = Path(get_settings().dataset_path)
WRITING_PATH = Path(get_settings().writing_path)
FILE_ROOT_PATH = Path(get_settings().root)


class BucketParser:
    def __init__(self, root: Path):
        self.root = root
        self.datasets = {}
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

            validator = MetadataValidator()
            if validator.validate(bucket / parser.parent / "METADATA"):
                logger.info(f"'{parser.parent}' metadata validated successfully.")

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
