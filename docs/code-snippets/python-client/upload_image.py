import logging
import sys
from argparse import ArgumentParser

import os

from cytomine import Cytomine
from cytomine.models import StorageCollection, Project, UploadedFile

if __name__ == '__main__':
    parser = ArgumentParser(prog="Upload image example")
    parser.add_argument('--host', help="The Cytomine host")
    parser.add_argument('--public_key', help="The Cytomine public key")
    parser.add_argument('--private_key', help="The Cytomine private key")
    parser.add_argument('--id_project', required=False, help="The project where to add the uploaded image")
    parser.add_argument('--filepath', help="The filepath (on your file system) of the file you want to upload")
    params, other = parser.parse_known_args(sys.argv[1:])

    with Cytomine(host=params.host, public_key=params.public_key, private_key=params.private_key,
                  verbose=logging.INFO) as cytomine:

        # Check that the file exists on your file system
        if not os.path.exists(params.filepath):
            raise ValueError("The file you want to upload does not exist")

        # Check that the given project exists
        if params.id_project:
            project = Project().fetch(params.id_project)
            if not project:
                raise ValueError("Project not found")

        # To upload the image, we need to know the ID of your Cytomine storage.
        storages = StorageCollection().fetch()
        my_storage = next(filter(lambda storage: storage.user == cytomine.current_user.id, storages))
        if not my_storage:
            raise ValueError("Storage not found")

        uploaded_file = cytomine.upload_image(upload_host=params.upload_host,
                                              filename=params.filepath,
                                              id_storage=my_storage.id,
                                              id_project=params.id_project)

        print(uploaded_file)