# -*- coding: utf-8 -*-

import logging
import sys
from argparse import ArgumentParser


from cytomine import Cytomine
from cytomine.models import AnnotationCollection

logging.basicConfig()
logger = logging.getLogger("cytomine.client")
logger.setLevel(logging.INFO)

if __name__ == '__main__':
    parser = ArgumentParser(prog="Cytomine Python client example")

    # Cytomine
    parser.add_argument('--cytomine_host', dest='host',
                        default='demo.cytomine.be', help="The Cytomine host")
    parser.add_argument('--cytomine_public_key', dest='public_key',
                        help="The Cytomine public key")
    parser.add_argument('--cytomine_private_key', dest='private_key',
                        help="The Cytomine private key")

    parser.add_argument('--cytomine_id_image_instance', dest='id_image_instance',
                        help="The image with annotations to delete")
    parser.add_argument('--cytomine_id_user', dest='id_user',
                        help="The user with annotations to delete")
    parser.add_argument('--cytomine_id_project', dest='id_project',
                        help="The project with annotations to delete")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key) as cytomine:

        # Get the list of annotations
        annotations = AnnotationCollection()
        annotations.image = params.id_image_instance
        annotations.user = params.id_user
        annotations.project = params.id_project
        annotations.fetch()
        print(annotations)

        for annotation in annotations:
            annotation.delete()
