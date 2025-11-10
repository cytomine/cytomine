from typing import Optional
from cytomine.models import (
    ImageInstance,
    ImageInstanceCollection,
    Ontology,
    OntologyCollection,
    Project,
    Term,
    TermCollection,
)


def get_project(key: str, projects: dict[str, Project]) -> Project:
    return projects.setdefault(key, Project(name=key).save())


def get_image(name: str, images: ImageInstanceCollection) -> Optional[ImageInstance]:
    return next((img for img in images if img.instanceFilename == name), None)


def get_ontology(name: str, ontologies: OntologyCollection) -> Optional[Ontology]:
    return next((o for o in ontologies if o.name == name), None)


def get_term(name: str, terms: TermCollection) -> Optional[Term]:
    return next((t for t in terms if t.name == name), None)
