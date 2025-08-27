import logging
import sys
from argparse import ArgumentParser

import os

from shapely import wkt
from shapely.affinity import affine_transform

from cytomine import Cytomine
from cytomine.models import AnnotationCollection, ImageInstanceCollection

def get_by_id(haystack, needle):
    return next((item for item in haystack if item.id == needle), None)

if __name__ == '__main__':
    parser = ArgumentParser(prog="Get annotations example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")
    parser.add_argument('--id_project', help="The project from which we want the annotations")
    parser.add_argument('--download_path', required=False, 
                        help="Where to store annotation crops. Required if you want Cytomine generate annotation crops.")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key, verbose=logging.INFO) as cytomine:
        # We want all annotations in a given project.
        annotations = AnnotationCollection()
        annotations.project = params.id_project  # Add a filter: only annotations from this project
        # You could add other filters:
        # annotations.image = id_image => Add a filter: only annotations from this image
        # annotations.images = [id1, id2] => Add a filter: only annotations from these images
        # annotations.user = id_user => Add a filter: only annotations from this user
        # ...
        annotations.showWKT = True  # Ask to return WKT location (geometry) in the response
        annotations.showMeta = True  # Ask to return meta information (id, ...) in the response
        annotations.showGIS = True  # Ask to return GIS information (perimeter, area, ...) in the response
        # ...
        # => Fetch annotations from the server with the given filters.
        annotations.fetch()
        print(annotations)

        for annotation in annotations:
            print("ID: {} | Image: {} | Project: {} | Term: {} | User: {} | Area: {} | Perimeter: {} | WKT: {}".format(
                annotation.id,
                annotation.image,
                annotation.project,
                annotation.term,
                annotation.user,
                annotation.area,
                annotation.perimeter,
                annotation.location
            ))

            # Annotation location is the annotation geometry in WKT format.
            # See https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry

            # You can use Shapely library to read geometry in WKT format. See https://shapely.readthedocs.io/en/latest/
            # See 'shapely.wkt.loads(wkt)' function in Shapely library.
            geometry = wkt.loads(annotation.location)
            print("Geometry from Shapely: {}".format(geometry))

            # In Cytomine, geometries are referenced using a cartesian coordinate system !
            # See 'shapely.affinity.affine_transform(geom, matrix)' function in Shapely library if needed

            if params.download_path:
                # max_size is set to 512 (in pixels). Without max_size parameter, it downloads a dump of the same size as the annotation.
                # Dump a rectangular crop containing the annotation
                annotation.dump(dest_pattern=os.path.join(params.download_path, "{project}", "crop", "{id}.jpg"), max_size=512)
                # Dumps a rectangular mask containing the annotation
                annotation.dump(dest_pattern=os.path.join(params.download_path, "{project}", "mask", "{id}.jpg"), mask=True, max_size=512)
                # Dumps the annotation crop where pixels outside it are transparent.
                annotation.dump(dest_pattern=os.path.join(params.download_path, "{project}", "alpha", "{id}.png"), mask=True, alpha=True, max_size=512)
