import geojson
import json
import logging
from lxml import etree
from pathlib import Path
from shapely.geometry import shape
from shapely import wkt
from typing import List

from cytomine.models import (
    Annotation,
    AnnotationTerm,
    ImageInstanceCollection,
    OntologyCollection,
    TermCollection,
)

from pims.importer.utils import get_image, get_ontology, get_term
from pims.schemas.annotation import WktAnnotation

logger = logging.getLogger("pims.app")


class AnnotationImporter:
    def __init__(
        self,
        base_path: Path,
        images: ImageInstanceCollection,
        ontologies: OntologyCollection,
    ) -> None:
        self.base_path = base_path
        self.annotations = {}
        self.images = images
        self.ontologies = ontologies

    def get_annotations(self) -> List[str]:
        dataset_xml_path = self.base_path / "METADATA" / "dataset.xml"
        tree = etree.parse(dataset_xml_path)
        root = tree.getroot()

        annotations = root.findall(".//ANNOTATION_REF")
        return [annot.get("alias") for annot in annotations]

    def load(self, annotation_path: Path) -> List[WktAnnotation]:
        try:
            with open(self.base_path / annotation_path, "r") as fp:
                geometry = geojson.load(fp)

            return geojson_to_wkt(geometry)
        except (json.JSONDecodeError, FileNotFoundError):
            logger.warning(f"'{annotation_path}' is not a valid geometry")
            return []

    def import_annotation(self, alias: str) -> None:
        file = self.annotations[alias].find(".//FILE")
        annotation_path = self.base_path / file.get("filename")
        image_name = self.annotations[alias].find(".//IMAGE_REF").get("alias")
        image = get_image(image_name, self.images)
        if image is None:
            logger.warning(f"Image {image_name} doesn't exist!")
            return

        ontology_name = self.annotations[alias].find(".//ONTOLOGY_REF").get("alias")
        ontology = get_ontology(ontology_name, self.ontologies)
        if ontology is None:
            logger.warning(f"Ontology {ontology_name} doesn't exist!")
            return

        terms = TermCollection().fetch_with_filter("ontology", ontology.id)
        annotations = self.load(annotation_path)
        for annotation in annotations:
            annot = Annotation(location=annotation.wkt, id_image=image.id).save()

            term_name = annotation.properties.get("path_class_name")
            term = get_term(term_name, terms)
            if term:
                AnnotationTerm(id_annotation=annot.id, id_term=term.id).save()

    def run(self):
        logger.info("[START] Import annotations...")
        annotation_xml_path = self.base_path / "METADATA" / "annotation.xml"
        tree = etree.parse(annotation_xml_path)
        root = tree.getroot()

        annotations = root.findall("ANNOTATION")
        self.annotations = {annot.get("alias"): annot for annot in annotations}

        for annotation in self.get_annotations():
            self.import_annotation(annotation)
        logger.info("[END] Import annotations...")


def feature_to_wkt(feature: dict) -> WktAnnotation:
    return WktAnnotation(
        wkt=wkt.dumps(shape(feature["geometry"])),
        properties=feature.get("properties", {}),
    )


def geojson_to_wkt(geojson) -> List[WktAnnotation]:
    geojson_type = geojson.get("type")

    if geojson_type == "FeatureCollection":
        return [feature_to_wkt(f) for f in geojson.get("features", [])]

    if geojson_type == "Feature":
        return [feature_to_wkt(geojson)]

    return [WktAnnotation(wkt=wkt.dumps(shape(geojson)), properties={})]
