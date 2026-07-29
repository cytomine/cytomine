# -*- coding: utf-8 -*-

import logging
import sys
from argparse import ArgumentParser

from cytomine import Cytomine
from cytomine.models import TermCollection

# This example script allows you to get the list of terms (labels) in a project.

logging.basicConfig()
logger = logging.getLogger("cytomine.client")
logger.setLevel(logging.INFO)

if __name__ == '__main__':
    parser = ArgumentParser(prog="Cytomine Python client example")

    # Cytomine connection parameters
    parser.add_argument('--cytomine_host', dest='host',
                        default='demo.cytomine.be', help="The Cytomine host")
    parser.add_argument('--cytomine_public_key', dest='public_key',
                        help="The Cytomine public key")
    parser.add_argument('--cytomine_private_key', dest='private_key',
                        help="The Cytomine private key")

    # Cytomine project ID
    parser.add_argument('--cytomine_id_project', dest='id_project',
                        help="The project from which we want the images")

    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key) as cytomine:

        terms = TermCollection().fetch_with_filter("project", params.id_project)
        for term in terms:
            print(f"Term ID: {term.id} | Name: {term.name} | Color: {term.color}")
