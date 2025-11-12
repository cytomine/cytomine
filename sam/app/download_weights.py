import os
import urllib.request

from app.config import get_settings

WEIGHTS = {
    "weights.pt": "https://huggingface.co/TVM13/Cytomine-sam/resolve/main/weights.pt"
}

WEIGHTS_PATH = get_settings().WEIGHTS_PATH

def download_weights():
    os.makedirs(WEIGHTS_PATH, exist_ok=True)

    for filename, url in WEIGHTS.items():
        destination = os.path.join(WEIGHTS_PATH, filename)

        if not os.path.exists(destination):
            print(f"Downloading {filename}...")
            urllib.request.urlretrieve(url, destination)
        else:
            print(f"{filename} already exists.")
