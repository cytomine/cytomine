import logging
import sys
from argparse import ArgumentParser

import os

from cytomine import Cytomine
from cytomine.models.image import ImageInstanceCollection

if __name__ == '__main__':
    parser = ArgumentParser(prog="Get images example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")
    parser.add_argument('--id_project', help="The project from which we want the images")
    parser.add_argument('--download_path', required=False, help="Where to store images")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key,
                  verbose=logging.INFO) as cytomine:

        # We want all image instances in a given project.
        # => Fetch the collection of image instances, filtered by the given project.
        image_instances = ImageInstanceCollection().fetch_with_filter("project", params.id_project)
        print(image_instances)

        for image in image_instances:
            # Every element in the collection is an ImageInstance object.
            # See ImageInstance class for all available properties (width, height, resolution, ...)
            print("Image ID: {} | Width: {} | Height: {} | Resolution: {} | Magnification: {} | Filename: {}".format(
                image.id, image.width, image.height, image.resolution, image.magnification, image.filename
            ))

            if params.download_path:
                # To download the original files that have been uploaded to Cytomine
                # Attributes of ImageInstance are parsed in the filename
                # Image is downloaded only if it does not exists locally or if override is True
                image.download(os.path.join(params.download_path, str(params.id_project), "{originalFilename}"),
                               override=False)