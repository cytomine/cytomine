import json
from typing import Optional
from lxml import etree
from pathlib import Path

from cytomine.models import Ontology, OntologyCollection, Term


class OntologyImporter:
    def __init__(self, ontology_path: Path) -> None:
        self.ontology_path = ontology_path

    def get_ontology(self, name: str) -> Optional[Ontology]:
        ontologies = OntologyCollection().fetch()
        matched = (ontology for ontology in ontologies if ontology.name == name)
        return next(matched, None)

    def load(self) -> Ontology:
        tree = etree.parse(self.ontology_path)
        root = tree.getroot()

        ontology_xml = root.find(".//ONTOLOGY")
        ontology_name = ontology_xml.get("alias")
        file = root.find(".//FILE")

        file_path = self.ontology_path / file.get("filename")
        with open(file_path, "r") as fp:
            ontology_data = json.load(fp)

        ontology = self.get_ontology(ontology_name)
        if ontology is not None:
            return ontology

        ontology = Ontology(ontology_name).save()
        for term in ontology_data:
            Term(term.get("type"), ontology.id, term.get("color")).save()

        return ontology
