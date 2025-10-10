import geojson
import json
from pathlib import Path

from cytomine.models import Annotation


class AnnotationImporter:
    def __init__(self, annotation_path: Path):
        self.annotation_path = annotation_path

    def load(self) -> Annotation | None:
        try:
            with open(self.annotation_path, "r") as fp:
                geometry = geojson.load(fp)

            return Annotation(location=geometry)
        except json.JSONDecodeError:
            return None
