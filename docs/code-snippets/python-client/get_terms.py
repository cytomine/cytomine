import logging
import sys
from argparse import ArgumentParser

from cytomine import Cytomine
from cytomine.models import TermCollection

if __name__ == '__main__':
    parser = ArgumentParser(prog="List terms in project example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")
    parser.add_argument('--id_project', help="The project from which we want the terms")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key,
                  verbose=logging.INFO) as cytomine:

        terms = TermCollection().fetch_with_filter("project", params.id_project)
        for term in terms:
            print("Term ID: {} | Name: {} | Color: {}".format(term.id, term.name, term.color))