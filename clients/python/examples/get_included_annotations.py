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
                        help="The image in which we work")
    parser.add_argument('--cytomine_id_roi_term', dest='id_roi_term',
                        help="The term that represents regions of interest")
    parser.add_argument('--cytomine_id_object_term', dest='id_object_term',
                        help="The term that represents objects")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key) as cytomine:
        roi_annotations = AnnotationCollection()
        roi_annotations.image = params.id_image_instance
        roi_annotations.term = params.id_roi_term
        roi_annotations.fetch()
        print(roi_annotations)

        for roi_annotation in roi_annotations:
            included_annotations = AnnotationCollection()
            included_annotations.image = params.id_image_instance
            included_annotations.term = params.id_object_term
            included_annotations.annotation = roi_annotation.id
            included_annotations.fetch()
            print(
                f"Number of annotations of term {params.id_object_term} included "
                f"in ROI {roi_annotation.id}: {len(included_annotations)}"
            )
