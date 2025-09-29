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

    def validate(self, xml_path) -> bool:
        try:
            xml_doc = etree.parse(xml_path)
            return self.schema.validate(xml_doc)
        except Exception as e:
            logger.error(f"Error: {e}")
            return False


class Validator:
    def __init__(self, schema_root: str) -> None:
        self.schema_root = Path(schema_root)

    def validate(self, file_path: Path, structure: MetadataStructure) -> bool:
        schema = self.schema_root / f"BP.{structure.value}.xsd"
        if not schema.exists():
            logger.error(f"Schema file not found: {schema}")
            return False

        validator = XMLValidator(schema)
        document_path = file_path / f"{structure.value}.xml"
        return validator.validate(document_path)


class MetadataValidator:
    def __init__(self) -> None:
        self.validator = Validator(self.schema_root)

    @property
    def schema_root(self) -> Path:
        return resources.files("resources") / "bigpicture_metaflex" / "src"

    def validate(self, file_path: Path) -> bool:
        return all(
            self.validator.validate(file_path, structure)
            for structure in MetadataStructure
        )
