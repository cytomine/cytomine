import logging
import sys
from argparse import ArgumentParser

from shapely.geometry import Point, box

from cytomine import Cytomine
from cytomine.models import Property, Annotation, AnnotationTerm, AnnotationCollection

if __name__ == '__main__':
    parser = ArgumentParser(prog="Add annotation example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key',help="The Cytomine private key")

    parser.add_argument('--id_project', help="The project from which we want the images")
    parser.add_argument('--id_image_instance', help="The image to which the annotation will be added")
    parser.add_argument('--id_term', required=False, help="The term to associate to the annotations (optional)")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key, verbose=logging.INFO) as cytomine:
        # We first add a point in (10,10) where (0,0) is bottom-left corner
        point = Point(10, 10)
        annotation_point = Annotation(location=point.wkt, id_image=params.id_image_instance).save()
        if params.id_term:
            AnnotationTerm(annotation_point.id, params.id_term).save()

        # Then, we add a rectangle as annotation
        rectangle = box(20, 20, 100, 100)
        annotation_rectangle = Annotation(location=rectangle.wkt, id_image=params.id_image_instance).save()
        if params.id_term:
            AnnotationTerm(annotation_rectangle.id, params.id_term).save()

        # We can also add a property (key-value pair) to an annotation
        Property(annotation_rectangle, key="my_property", value=10).save()

        # Print the list of annotations in the given image:
        annotations = AnnotationCollection()
        annotations.image = params.id_image_instance
        annotations.fetch()
        print(annotations)

        # We can also add multiple annotation in one request:
        annotations = AnnotationCollection()
        annotations.append(Annotation(location=point.wkt, id_image=params.id_image_instance, id_project=params.id_project))
        annotations.append(Annotation(location=rectangle.wkt, id_image=params.id_image_instance, id_project=params.id_project))
        annotations.save()

        # Print the list of annotations in the given image:
        annotations = AnnotationCollection()
        annotations.image = params.id_image_instance
        annotations.fetch()
        print(annotations)