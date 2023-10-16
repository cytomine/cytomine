from PIL import Image
import os
import urllib.request
import io

from tests.utils.formats import thumb_test, resized_test, mask_test
from tests.utils.formats import crop_test, crop_null_annot_test

from pims.formats import FORMATS  # noqa: F401
from pims.files.archive import Archive
from pims.files.file import EXTRACTED_DIR, HISTOGRAM_STEM, ORIGINAL_STEM, PROCESSED_DIR  # noqa: F401
from pims.files.file import SPATIAL_STEM, UPLOAD_DIR_PREFIX,  Path  # noqa: F401

from pims.importer.importer import FileImporter
import pytest


def get_image(path, image):
    filepath = os.path.join(path, image)
    # If image does not exist locally -> download image
    if not os.path.exists(path):
        os.mkdir("/data/pims/upload_test_czi")
    
    if not os.path.exists(filepath):
        try:
            url = f"https://data.cytomine.coop/open/uliege/{image}" #OAC
            urllib.request.urlretrieve(url, filepath)
        except Exception as e:
            print("Could not download image")
            print(e)
    
    if not os.path.exists(os.path.join(path, "processed")):
        try:
            fi = FileImporter(f"/mnt/c/Users/yba/pims/Root/upload_test_czi/{image}")
            fi.upload_dir = "/mnt/c/Users/yba/pims/Root/upload_test_czi"
            fi.processed_dir = fi.upload_dir / Path("processed")
            fi.mkdir(fi.processed_dir)
        except Exception as e:
            print(path + "processed could not be created")
            print(e)
    if not os.path.exists(os.path.join(path, "processed/visualisation.CZI")):
        try:
            fi.upload_path = Path(filepath)
            original_filename = Path(f"{ORIGINAL_STEM}.CZI")
            fi.original_path = fi.processed_dir / original_filename
            fi.mksymlink(fi.original_path, fi.upload_path)
            spatial_filename = Path(f"{SPATIAL_STEM}.CZI")
            fi.spatial_path = fi.processed_dir / spatial_filename
            fi.mksymlink(fi.spatial_path, fi.original_path)
        except Exception as e:
            print("Importation of images could not be done")
            print(e)



def test_czi_exists(image_path_czi):
    # Test if the file exists, either locally either with the OAC
    get_image(image_path_czi[0], image_path_czi[1])
    assert os.path.exists(os.path.join(image_path_czi[0], image_path_czi[1])) is True


def test_czi_info(client, image_path_czi):
    response = client.get(f'/image/upload_test_czi/{image_path_czi[1]}/info')
    assert response.status_code == 200
    assert "czi" in response.json()['image']['original_format'].lower()
    assert response.json()['image']['width'] == 52061
    assert response.json()['image']['height'] == 52061
    assert response.json()['image']['depth'] == 1
    assert response.json()['image']['duration'] == 1
    assert response.json()['image']['physical_size_x'] == 4.4
    assert response.json()['image']['physical_size_y'] == 4.4
    assert response.json()['image']['n_channels'] == 3
    assert response.json()['image']['n_concrete_channels'] == 1
    assert response.json()['image']['n_samples'] == 3
    assert response.json()['image']['are_rgb_planes'] == True
    assert response.json()['image']['pixel_type'] == "uint8"
    assert response.json()['image']['significant_bits'] == 8


def test_czi_metadata(client, image_path_czi):
    response = client.get(f'/image/upload_test_czi/{image_path_czi[1]}/metadata')
    assert response.status_code == 200
    print(response.json())
    assert response.json()['items'][0]['key'] == 'ImageDocument_Metadata_Experiment_@Version'
    assert response.json()['items'][0]["value"] == '1.2'
    assert response.json()['items'][5]['key'] == 'ImageDocument_Metadata_Experiment_IsSegmented'
    assert response.json()['items'][5]['value'] == 'false'
    assert response.json()['items'][6]['key'] == 'ImageDocument_Metadata_Experiment_IsStandardMode'
    assert response.json()['items'][6]['value'] == 'true'
    assert response.json()['items'][8]['key'] == 'ImageDocument_Metadata_Experiment_ImageTransferMode'
    assert response.json()['items'][8]['value'] == 'MemoryMappedAndFileStream'


# For a non-normalized tile, the width is 124
# To have a 256 x 256, the zoom level needs to be high enough
def test_czi_norm_tile(client, image_path_czi):
    response = client.get(f"/image/upload_test_czi/{image_path_czi[1]}/normalized-tile/zoom/4/ti/3",
                          headers={"accept": "image/jpeg"})
    assert response.status_code == 200

    img_response = Image.open(io.BytesIO(response.content))
    width_resp, height_resp = img_response.size
    assert width_resp == 256
    assert height_resp == 256


def test_czi_thumb(client, image_path_czi):
    thumb_test(client, image_path_czi[1], "czi")


def test_czi_resized(client, image_path_czi):
    resized_test(client, image_path_czi[1], "czi")


def test_czi_mask(client, image_path_czi):
    mask_test(client, image_path_czi[1], "czi")


def test_czi_crop(client, image_path_czi):
    crop_test(client, image_path_czi[1], "czi")


def test_czi_crop_null_annot(client, image_path_czi):
    crop_null_annot_test(client, image_path_czi[1], "czi")
