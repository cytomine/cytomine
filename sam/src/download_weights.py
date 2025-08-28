import os
import urllib.request

WEIGHTS = {
    "weights.pt": "https://huggingface.co/TVM13/Cytomine-sam/resolve/main/weights.pt"
}

os.makedirs("weights", exist_ok = True)

for filename, url in WEIGHTS.items():
    destination = os.path.join("weights", filename)

    if not os.path.exists(destination):
        print(f"Downloading {filename}...")
        urllib.request.urlretrieve(url, destination)

    else:
        print(f"{filename} already exists.")
