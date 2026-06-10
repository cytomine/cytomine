import logging
import json
import os
from collections import defaultdict
from collections.abc import Mapping
from lxml import etree
from typing import List, Any
from dataclasses import dataclass, fields, is_dataclass
from datetime import datetime
from enum import Enum
import uuid

from cytomine import Cytomine
from cytomine.models import (
    ImageInstanceCollection,
    OntologyCollection,
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
from pims.importer.utils import get_project
from pims.schemas.auth import ApiCredentials, CytomineAuth
from pims.schemas.operations import ImportResponse, ImportSummary

from bigpicture_metadata_interface import BPInterface
from bigpicture_metadata_interface.model.dataset import Dataset
from bigpicture_metadata_interface.model.image import Image
from bigpicture_metadata_interface.model.sample import (
    BiologicalBeing,
    Block,
    Slide,
    Specimen,
)
from bigpicture_metadata_interface.model.observation import Observation
from bigpicture_metadata_interface.model.stain import StainingList, Stain
from bigpicture_metadata_interface.model.common import Code, CodeAttributes, Attributes

logger = logging.getLogger("pims.app")

DATASET_ROOT = Path(get_settings().dataset_path)
WRITING_PATH = Path(get_settings().writing_path)
FILE_ROOT_PATH = Path(get_settings().root)


class BucketParser:
    def __init__(self, root: Path) -> None:
        self.root = root
        self.datasets = {}
        self.dependency = defaultdict(list)

    @property
    def parent(self) -> str:
        return next(iter(self.dependency.keys()), None) or next(iter(self.datasets.keys()))

    @property
    def children(self) -> list[str]:
        return self.dependency.get(self.parent, [])

    def discover(self) -> None:
        for child in self.root.iterdir():
            if not child.is_dir():
                logger.warning(f"'{child}' is not a folder!")
                logger.warning(f"Skipping '{child}' ...")
                continue

            metadata_path = child / "METADATA"
            if not metadata_path.exists():
                logger.warning(f"'{metadata_path}' does not exist!")
                logger.warning(f"Skipping '{child}' ...")
                continue

            metadata_dataset_path = metadata_path / "dataset.xml"
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



def run_import_datasets(
    cytomine_auth: CytomineAuth,
    credentials: ApiCredentials,
    storage_id: str,
) -> ImportResponse:
    client = get_settings().meilisearch_client
    index = client.index("imageMetadataIndex")
    _configure_index(index)
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

        annotation_summary = {}
        image_summary = ImportSummary()
#         for bucket in buckets:
#             try:
#                 parser = BucketParser(bucket)
#                 parser.discover()
#
#                 if not parser.parent:
#                     logger.warning(f"No parent dataset found for {bucket}, skipping...")
#                     continue
#
#                 parent_dataset = parser.parent
#
#                 validator = MetadataValidator()
#                 if validator.validate(bucket / parent_dataset / "METADATA"):
#                     logger.info(f"'{parent_dataset}' metadata validated successfully.")
#
#                 project = get_project(parent_dataset, projects)
#
#                 image_summary = ImageImporter(
#                     bucket / parent_dataset,
#                     cytomine_auth,
#                     c.current_user,
#                     storage_id,
#                 ).run(projects=[project])
#
#                 images = ImageInstanceCollection().fetch_with_filter("project", project.id)
#                 ontologies = OntologyCollection().fetch()
#
#                 for child in parser.children:
#                     child_path = bucket / child
#                     ontology = OntologyImporter(child_path).run()
#                     ontologies.append(ontology)
#
#                     if project.ontology is None:
#                         project.ontology = ontology.id
#                         project.update()
#
#                     result = AnnotationImporter(child_path, images, ontologies).run()
#                     annotation_summary[child] = result
#             except Exception as e:
#                 logger.error(f"Failed to process bucket {bucket}: {e}", exc_info=True)
#                 continue

        try:
            indexing_payload = []
            parsed_dataset = BPInterface.parse_xml_files(DATASET_ROOT)
            slide_to_block = build_slide_to_block_map(parsed_dataset)
            counter = 0
            for image in parsed_dataset.images.values():
                counter += 1
                flat_dict = flatten_image(image, parsed_dataset, slide_to_block)
                flat_dict["id"] = flat_dict["image"]["identifier"]
                indexing_payload.append(flat_dict)
                if counter == 1:
                    logger.info(json.dumps(flat_dict, indent=2, default=str))

            batch_size = 200
            for i in range(0, len(indexing_payload), batch_size):
                batch = indexing_payload[i:i+batch_size]
                result = index.add_documents(batch)
                task = client.wait_for_task(result.task_uid)
                logger.info(f"Indexed batch {i//batch_size + 1} and the status is {task.status}")
                if task.status == "failed":
                    logger.info(f"Error details: {task.error}")
        except Exception as e:
            logger.error(f"Failed to parse xml files {e}", exc_info=True)
        return ImportResponse(
            image_summary=image_summary,
            annotation_summary=annotation_summary,
        )

def dataclass_to_dict(obj: Any):
    """Recursively convert dataclass objects (and nested structures) to plain dicts.
    Handles Code, CodeAttributes, Attributes, CustomAttributes, lists, dicts, enums, dates, UUIDs.
    """
    if obj is None:
        return None
    # Primitives
    if isinstance(obj, (str, int, float, bool)):
        return obj
    # Enums
    if isinstance(obj, Enum):
        return obj.value
    # Datetime
    if isinstance(obj, datetime):
        return obj.isoformat()
    # UUID
    if isinstance(obj, uuid.UUID):
        return str(obj)
    # Code object
    if isinstance(obj, Code):
        return {
            "code": obj.code,
            "scheme": obj.scheme,
            "meaning": obj.meaning,
            "scheme_version": obj.scheme_version,
        }
    # CodeAttributes (which is a dict-like mapping from str to Code)
    if isinstance(obj, CodeAttributes):
        return {key: dataclass_to_dict(value) for key, value in obj.items()}
    # Attributes / CustomAttributes (dict of str -> any)
    if isinstance(obj, Attributes):
        return {key: dataclass_to_dict(value) for key, value in obj.items()}
    # List or tuple
    if isinstance(obj, (list, tuple)):
        return [dataclass_to_dict(item) for item in obj]
    # Dict (handles any mapping, including from other libraries)
    if isinstance(obj, dict):
        return {key: dataclass_to_dict(value) for key, value in obj.items()}
    # Any other dataclass
    if is_dataclass(obj):
        result = {}
        for field in fields(obj):
            value = getattr(obj, field.name)
            # Skip internal/private fields (optional, adjust as needed)
            if field.name.startswith("_"):
                continue
            result[field.name] = dataclass_to_dict(value)
        return result
    # Fallback (should not happen)
    return str(obj)



def build_slide_to_block_map(dataset: Dataset) -> dict[Slide, Block]:
    """Return a mapping from Slide instances to the Block that contains them."""
    slide_to_block = {}
    for being in dataset.biological_beings.values():
        for specimen in being.specimens.values():
            for block in specimen.blocks.values():
                for slide in block.slides.values():
                    alias = slide.reference.alias
                    slide_to_block[alias] = block
    return slide_to_block

def flatten_image(image: Image, dataset: Dataset, slide_to_block: Mapping[Slide, Block]) -> dict:
    """Produce a flat dictionary containing the image and all its related metadata."""
    slide = image.slide
    block = slide_to_block.get(slide.reference.alias)
    if block is None:
        raise ValueError(f"Block not found for slide {slide.identifier}")

    # Find all observations that refer to any specimen in this block
    observations_by_specimen = {}
    for obs in dataset.observations.values():
        # Observation.item can be a Specimen (or other)
        if isinstance(obs.item, Specimen):
            alias = obs.item.reference.alias
            observations_by_specimen.setdefault(alias, []).append(obs)

    # Build list of specimens with their full context
    specimens_list = []
    # Better: iterate over all specimens in the dataset and check if the block is in specimen.blocks
    for being in dataset.biological_beings.values():
        for specimen in being.specimens.values():
            if block in specimen.blocks.values():
                # Build specimen dict
                specimen_dict = {
                    "alias": specimen.reference.alias if specimen.reference else None,
                    "identifier": specimen.identifier,
                    "specimen_type": dataclass_to_dict(specimen.specimen_type),
                    "extraction_method": dataclass_to_dict(specimen.extraction_method),
                    "fixation_type": dataclass_to_dict(specimen.fixation_type),
                    "anatomical_site": dataclass_to_dict(specimen.anatomical_site),
                    "anatomical_sites": [dataclass_to_dict(s) for s in specimen.anatomical_sites],
                    "age_at_extraction": dataclass_to_dict(specimen.age_at_extraction),
                    "biological_being": {
                        "alias": being.reference.alias if being.reference else None,
                        "identifier": being.identifier,
                        "sex": being.sex.value if being.sex else None,
                        "animal_species": dataclass_to_dict(being.animal_species),
                        "strain": dataclass_to_dict(being.attributes.get("STRAIN")) if being.attributes else None,
                        "disposition": dataclass_to_dict(being.attributes.get("DSDECOD")) if being.attributes else None,
                        "control_status": dataclass_to_dict(being.attributes.get("control_terminology")) if being.attributes else None,
                    },
                    "observations": []
                }
                # Add observations for this specimen
                for obs in observations_by_specimen.get(specimen.reference.alias, []):
                    observer_list = []
                    for observer in obs.observers:
                        observer_list.append({
                            "alias": observer.reference.alias if observer.reference else None,
                            "identifier": observer.identifier,
                            "observer_type": observer.observer_type.value if observer.observer_type else None,
                        })
                    specimen_dict["observations"].append({
                        "observation_alias": obs.reference.alias if obs.reference else None,
                        "identifier": obs.identifier,
                        "statement_type": obs.statement.statement_type.value if obs.statement.statement_type else None,
                        "statement_status": obs.statement.statement_status.value if obs.statement.statement_status else None,
                        "code_attributes": dataclass_to_dict(obs.statement.code_attributes),
                        "custom_attributes": dataclass_to_dict(obs.statement.custom_attributes),
                        "freetext": obs.statement.freetext,
                        "observers": observer_list,
                    })
                specimens_list.append(specimen_dict)

    # Build flat structure
    flat = {
        "image": dataclass_to_dict(image),
        "slide": {
            "alias": slide.reference.alias if slide.reference else None,
            "identifier": slide.identifier,
            "staining": _extract_staining_info(slide.staining_information),
        },
        "block": {
            "alias": block.reference.alias if block.reference else None,
            "identifier": block.identifier,
            "block_preparation": dataclass_to_dict(block.block_preparation),
        },
        "specimens": specimens_list,
        "dataset": {
            "alias": dataset.reference.alias if dataset.reference else None,
            "accession": dataset.identifier,
            "title": dataset.title,
            "description": dataset.description,
            "study_duration_days": dataset.tox_study_duration.total_seconds() / 86400 if dataset.tox_study_duration else None,
            "metadata_standard": dataset.metadata_standard,
        },
        "policy": dataclass_to_dict(dataset.policy) if dataset.policy else None,
    }
    return flat

def _extract_staining_info(staining):
    """Extract staining details from StainingProcedure or StainingList."""
    if staining is None:
        return None
    if hasattr(staining, "procedure"):  # StainingProcedure
        return {"procedure": dataclass_to_dict(staining.procedure)}
    elif hasattr(staining, "stains"):   # StainingList
        stains_list = []
        for stain in staining.stains:
            if hasattr(stain, "compound"):  # ChemicalStain
                stains_list.append({
                    "type": "chemical",
                    "compound": dataclass_to_dict(stain.compound),
                })
            elif hasattr(stain, "target"):  # TargetedStain (Immunogenic, InSituHybridisation)
                stain_dict = {
                    "type": "targeted",
                    "compound": stain.compound.value if isinstance(stain.compound, Enum) else stain.compound,
                    "target": dataclass_to_dict(stain.target),
                    "reporter_type": stain.reporter_type.value if stain.reporter_type else None,
                    "reporter_color": stain.reporter_color.value if stain.reporter_color else None,
                }
                if hasattr(stain, "antibody_information") and stain.antibody_information:
                    stain_dict["antibody"] = dataclass_to_dict(stain.antibody_information)
                if hasattr(stain, "probe"):
                    stain_dict["probe"] = dataclass_to_dict(stain.probe)
                stains_list.append(stain_dict)
        return {"stains": stains_list}
    else:
        return {"raw": dataclass_to_dict(staining)}

def _configure_index(index) -> None:
    """Configure searchable and filterable attributes on the MeiliSearch index."""
    searchable_attributes = [
        # Image identifiers
        "image.identifier",
        "image.uid",
        "image.reference.alias",

        # Slide identifiers
        "slide.identifier",
        "slide.alias",

        # Block identifiers
        "block.identifier",
        "block.alias",

        # Specimen & biological being identifiers
        "specimens.identifier",
        "specimens.alias",
        "specimens.biological_being.identifier",
        "specimens.biological_being.alias",
        "specimens.biological_being.strain.meaning",        # e.g., "WISTAR HAN"
        "specimens.biological_being.animal_species.meaning", # e.g., "RAT"
        "specimens.biological_being.sex",                   # "Male"/"Female"
        "specimens.biological_being.control_status.meaning", # "Controlled"/"Treated"
        "specimens.biological_being.disposition.meaning",    # e.g., "TERMINAL SACRIFICE"

        # Anatomical site
        "specimens.anatomical_site.meaning",                # e.g., "KIDNEY", "BONE MARROW"

        # Observations
        "specimens.observations.identifier",
        "specimens.observations.observation_alias",
        "specimens.observations.code_attributes.MISTRESC.meaning",  # e.g., "UNREMARKABLE"
        "specimens.observations.code_attributes.MITEST.meaning",    # e.g., "General Histopathologic Exam, Qual"
        "specimens.observations.custom_attributes.MIORRES",         # free text: "No Abnormalities Detected"

        # Observers
        "specimens.observations.observers.identifier",
        "specimens.observations.observers.alias",
        "specimens.observations.observers.observer_type",           # "Human"

        # Dataset
        "dataset.title",
        "dataset.description",
        "dataset.accession",
        "dataset.alias",

        # Policy (if you want users to search by policy terms)
        "policy.identifier",
        "policy.type_of_dataset",
        "policy.allowed_geographical_distribution",
    ]
    filterable_attributes = [
        "specimens.biological_being.animal_species.meaning",
        "specimens.anatomical_site.meaning",
        "specimens.biological_being.sex",
        "specimens.age_at_extraction.interval_start",
        "slide.staining.stains.compound.meaning",
        "specimens.fixation_type.meaning",
        "block.block_preparation.meaning",
        "specimens.specimen_type.meaning",
    ]
    index.update_settings({
        "searchableAttributes": searchable_attributes,
        "filterableAttributes": filterable_attributes,
    })

