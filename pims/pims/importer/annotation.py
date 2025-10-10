import geojson
import json
from pathlib import Path

from cytomine.models import Annotation


class AnnotationImporter:
    def __init__(self, base_path: Path):
        self.base_path = base_path

    def load(self, annotation_path: Path) -> Annotation | None:
        try:
            with open(self.base_path / annotation_path, "r") as fp:
                geometry = geojson.load(fp)

            return Annotation(location=geometry)
        except (json.JSONDecodeError, FileNotFoundError):
            return None
