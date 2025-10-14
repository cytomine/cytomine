import geojson
import json
import logging
from lxml import etree
from pathlib import Path
from shapely.geometry import shape
from shapely import wkt

from cytomine.models import Annotation, ImageInstanceCollection


logger = logging.getLogger("pims.app")


class AnnotationImporter:
    def __init__(self, base_path: Path, images: ImageInstanceCollection):
        self.base_path = base_path
        self.annotations = {}
        self.images = images

    def get_annotations(self):
        dataset_xml_path = self.base_path / "METADATA" / "dataset.xml"
        tree = etree.parse(dataset_xml_path)
        root = tree.getroot()

        annotations = root.findall(".//ANNOTATION_REF")
        return [annot.get("alias") for annot in annotations]

    def load(self, annotation_path: Path):
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
            return

        annotations = self.load(annotation_path)
        for annotation in annotations:
            Annotation(location=annotation.get("wkt"), id_image=image.id).save()

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


def get_image(name: str, images: ImageInstanceCollection):
    for image in images:
        if image.instanceFilename == name:
            return image

    return None


def geojson_to_wkt(geojson):
    if geojson.get("type") == "FeatureCollection":
        results = []
        for feature in geojson.get("features", []):
            geom = shape(feature["geometry"])
            results.append(
                {
                    "wkt": wkt.dumps(geom),
                    "properties": feature.get("properties", {}),
                }
            )
        return results

    if geojson.get("type") == "Feature":
        geom = shape(geojson["geometry"])
        return [{"wkt": wkt.dumps(geom), "properties": geojson.get("properties", {})}]

    geom = shape(geojson)
    return [{"wkt": wkt.dumps(geom), "properties": {}}]
