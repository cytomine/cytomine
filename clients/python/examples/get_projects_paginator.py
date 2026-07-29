# -*- coding: utf-8 -*-

import logging
import sys
from argparse import ArgumentParser

from cytomine import Cytomine
from cytomine.models import ProjectCollection

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

    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key) as cytomine:
        """
        Number of results per page.
        """
        max = 10

        """
        Offset in the results (where to start).
        """
        offset = 0

        projects = ProjectCollection(max=max, offset=offset)
        while True:
            projects.fetch_next_page()
            print(projects)

            if not projects.fetch_next_page():
                break
