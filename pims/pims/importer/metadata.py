import logging
from enum import Enum
from importlib import resources
from lxml import etree
from pathlib import Path


logger = logging.getLogger("pims.app")


class MetadataStructure(Enum):
    DATASET = "dataset"
    IMAGE = "image"
    OBSERVATION = "observation"
    POLICY = "policy"
    SAMPLE = "sample"
    STAINING = "staining"


class XMLValidator:
    def __init__(self, schema_path: Path) -> None:
        tree = etree.parse(schema_path)
        self.schema = etree.XMLSchema(tree)

    def validate(self, xml_path: Path) -> bool:
        try:
            xml_doc = etree.parse(xml_path)
            return self.schema.validate(xml_doc)
        except Exception as e:
            logger.error(f"Error: {e}")
            return False


class MetadataValidator:
    @property
    def schema_root(self) -> Path:
        return resources.files("resources") / "bigpicture_metaflex" / "src"

    def validate(self, file_path: Path) -> bool:
        for structure in MetadataStructure:
            schema_path = self.schema_root / f"BP.{structure.value}.xsd"
            if not schema_path.exists():
                logger.error(f"Schema file not found: {schema_path}")
                return False

            validator = XMLValidator(schema_path)
            document_path = file_path / f"{structure.value}.xml"
            if not validator.validate(document_path):
                logger.error(f"Document file not valid: {document_path}")
                return False

        return True
