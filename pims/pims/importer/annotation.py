import geojson
import json
import logging
from lxml import etree
from pathlib import Path
from shapely import wkt
from shapely.geometry import shape
from shapely.ops import transform
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
from pims.schemas.operations import ImportResult, ImportSummary

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

    def load(self, annotation_path: Path, image_height: int) -> List[WktAnnotation]:
        try:
            with open(self.base_path / annotation_path, "r") as fp:
                geometry = geojson.load(fp)

            return geojson_to_wkt(geometry, image_height)
        except (json.JSONDecodeError, FileNotFoundError):
            logger.debug(f"'{annotation_path}' is not a valid geometry")
            return []

    def import_annotation(self, alias: str) -> ImportResult:
        file = self.annotations[alias].find(".//FILE")
        annotation_path = self.base_path / file.get("filename")
        image_name = self.annotations[alias].find(".//IMAGE_REF").get("alias")
        image = get_image(image_name, self.images)
        if image is None:
            logger.debug(f"Image {image_name} does not exist!")
            return ImportResult(
                name=alias,
                success=False,
                message=f"Image {image_name} does not exist",
            )

        ontology_name = self.annotations[alias].find(".//ONTOLOGY_REF").get("alias")
        ontology = get_ontology(ontology_name, self.ontologies)
        if ontology is None:
            logger.debug(f"Ontology {ontology_name} does not exist!")
            return ImportResult(
                name=alias,
                success=False,
                message=f"Ontology {ontology_name} does not exist",
            )

        terms = TermCollection().fetch_with_filter("ontology", ontology.id)
        annotations = self.load(annotation_path, image.height)
        for annotation in annotations:
            annot = Annotation(location=annotation.wkt, id_image=image.id).save()

            term_name = annotation.properties.get("path_class_name")
            term = get_term(term_name, terms)
            if term:
                AnnotationTerm(id_annotation=annot.id, id_term=term.id).save()

        return ImportResult(name=alias, success=True)

    def run(self) -> ImportSummary:
        logger.info("[START] Import annotations...")
        annotation_xml_path = self.base_path / "METADATA" / "annotation.xml"
        tree = etree.parse(annotation_xml_path)
        root = tree.getroot()

        annotations = root.findall("ANNOTATION")
        self.annotations = {annot.get("alias"): annot for annot in annotations}
        results = [self.import_annotation(annot) for annot in self.get_annotations()]
        successful = sum(1 for r in results if r.success)
        logger.info("[END] Import annotations...")

        return ImportSummary(
            total=len(results),
            successful=successful,
            failed=len(results) - successful,
            results=results,
        )


def feature_to_wkt(feature: dict, image_height: int) -> WktAnnotation:
    geometry = shape(feature["geometry"])
    flipped_geometry = transform(lambda x, y: (x, image_height - y), geometry)

    return WktAnnotation(
        wkt=wkt.dumps(flipped_geometry),
        properties=feature.get("properties", {}),
    )


def geojson_to_wkt(geojson: dict, image_height: int) -> List[WktAnnotation]:
    geojson_type = geojson.get("type")

    if geojson_type == "FeatureCollection":
        return [feature_to_wkt(f, image_height) for f in geojson.get("features", [])]

    if geojson_type == "Feature":
        return [feature_to_wkt(geojson, image_height)]

    return [WktAnnotation(wkt=wkt.dumps(shape(geojson)), properties={})]
