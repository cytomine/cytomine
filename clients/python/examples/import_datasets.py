"""Example to import datasets into Cytomine."""

import logging
import sys
from argparse import ArgumentParser

from urllib.parse import urljoin
from cytomine import Cytomine
from cytomine.models import StorageCollection

logging.basicConfig()
logger = logging.getLogger("cytomine.client")
logger.setLevel(logging.DEBUG)


if __name__ == "__main__":
    parser = ArgumentParser(description="Example to import datasets into Cytomine")

    parser.add_argument(
        "--cytomine_core_host",
        help="The Cytomine core host",
    )

    parser.add_argument(
        "--cytomine_core_external_host",
        help="The Cytomine external host (won't be called)",
    )

    parser.add_argument(
        "--cytomine_pims_host",
        help="The Cytomine pims host",
    )
    parser.add_argument(
        "--public_key",
        help="The Cytomine public key",
    )
    parser.add_argument(
        "--private_key",
        help="The Cytomine private key",
    )
    parser.add_argument(
        "--import-uri",
        default="import",
        help="The Cytomine private key",
    )
    params, _ = parser.parse_known_args(sys.argv[1:])
    logger.info("Before cytomine")
    with Cytomine(
        host=params.cytomine_core_host,
        public_key=params.public_key,
        private_key=params.private_key,
        real_url = params.cytomine_core_external_host,
    ) as cytomine:
        logger.info("In cytomine {cytomine}")
        # To import the datasets, we need to know the ID of your Cytomine storage.
        storages = StorageCollection().fetch()
        storage = next(
            filter(lambda storage: storage.user == cytomine.current_user.id, storages)
        )
        if not storage:
            raise ValueError("Storage not found")

        response = cytomine.import_datasets(storage.id, pims_url= urljoin(params.cytomine_pims_host, params.import_uri))

        print(response)
