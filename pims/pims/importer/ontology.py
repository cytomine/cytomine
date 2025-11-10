import json
import logging
from typing import Optional
from lxml import etree
from pathlib import Path

from cytomine.models import Ontology, OntologyCollection, Term

logger = logging.getLogger("pims.app")


class OntologyImporter:
    def __init__(self, base_path: Path) -> None:
        self.base_path = base_path

    def get_ontology(self, name: str) -> Optional[Ontology]:
        return next((o for o in OntologyCollection().fetch() if o.name == name), None)

    def run(self) -> Ontology:
        ontology_xml_path = self.base_path / "METADATA" / "ontology.xml"
        tree = etree.parse(ontology_xml_path)
        root = tree.getroot()

        ontology_xml = root.find(".//ONTOLOGY")
        ontology_name = ontology_xml.get("alias")
        file = root.find(".//FILE")

        file_path = self.base_path / file.get("filename")
        with open(file_path, "r") as fp:
            ontology_data = json.load(fp)

        ontology = self.get_ontology(ontology_name)
        if ontology is not None:
            logger.info(f"{ontology.name} already exists!")
            return ontology

        ontology = Ontology(ontology_name).save()
        for term in ontology_data:
            Term(term.get("type"), ontology.id, term.get("color")).save()

        return ontology
