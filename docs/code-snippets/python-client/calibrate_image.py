import logging
from argparse import ArgumentParser
from cytomine import Cytomine
from cytomine.models import ImageInstance, AbstractImage

if __name__ == '__main__':
    parser = ArgumentParser(prog="Set calibration of an image (note that all instances of the image will be affected)")
    parser.add_argument('--host', default='localhost-core', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")
    parser.add_argument('--id_image', help="The identifier of the image instance to calibrate")
    parser.add_argument('--resolution', required=False, help="The resolution to set, in µm/px (optional)")
    parser.add_argument('--magnification', required=False, help="The magnification to set (optional)")
    params, _ = parser.parse_known_args()

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key, verbose=logging.INFO) as cytomine:
        image_instance = ImageInstance().fetch(params.id_image) # Fetch the image instance
        abstract_image = AbstractImage().fetch(image_instance.baseImage) # Retrieve the abstract image
        modification = False
        if params.resolution is not None:
            abstract_image.resolution = params.resolution
            modification = True
        if params.magnification is not None:
            abstract_image.magnification = params.magnification
            modification = True
        if not modification:
            logging.error("You should set either resolution or magnification")
        else:
            abstract_image.update()