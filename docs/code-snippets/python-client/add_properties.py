import logging
import sys
from argparse import ArgumentParser

from cytomine import Cytomine
from cytomine.models import Property, Project, Annotation, ImageInstance

if __name__ == '__main__':
    parser = ArgumentParser(prog="Add properties example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")

    parser.add_argument('--key', help="the property key")
    parser.add_argument('--value', help="the property value")

    parser.add_argument('--id_project', required=False, help="The project to which the property will be added (optional)")
    parser.add_argument('--id_image_instance',, required=False, help="The image to which the property will be added (optional)")
    parser.add_argument('--id_annotation', required=False, help="The annotation to which the property will be added (optional)")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key, verbose=logging.INFO) as cytomine:

        if params.id_project:
            project = Project().fetch(params.id_project)
            prop = Property(project, key=params.key, value=params.value).save()
            print(prop)

        if params.id_image_instance:
            image = ImageInstance().fetch(params.id_image_instance)
            prop = Property(image, key=params.key, value=params.value).save()
            print(prop)

        if params.id_annotation:
            annot = Annotation().fetch(params.id_annotation)
            prop = Property(annot, key=params.key, value=params.value).save()
            print(prop)