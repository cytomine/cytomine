# -*- coding: utf-8 -*-

# * This script is just a simple example used to verify if the
# * Cytomine Python Client is correctly installed.
# * Using a correct Cytomine instance URL, and keys of a user, it will just fetch his username.

import sys
from argparse import ArgumentParser

from cytomine import Cytomine
from cytomine.models.user import CurrentUser

if __name__ == "__main__":
    parser = ArgumentParser(prog="Cytomine Python client example")

    # Cytomine
    parser.add_argument(
        "--cytomine_host",
        dest="host",
        default="demo.cytomine.be",
        help="The Cytomine host",
    )
    parser.add_argument(
        "--cytomine_public_key",
        dest="public_key",
        help="The Cytomine public key",
    )
    parser.add_argument(
        "--cytomine_private_key",
        dest="private_key",
        help="The Cytomine private key",
    )
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(
        host=params.host,
        public_key=params.public_key,
        private_key=params.private_key,
    ) as cytomine:
        # Get the connected user
        user = CurrentUser().fetch()
        print(user)
