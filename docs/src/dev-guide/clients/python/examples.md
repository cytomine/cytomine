---
title: API Client for Python Examples
---

# {{$frontmatter.title}}

This section provides code snippets for common use cases with the Cytomine API Client for Python library. More examples are available on the [GitHub repository](https://github.com/cytomine/Cytomine-python-client/tree/main/examples).

## Get images

This script fetches all images in a project with ID `id_project`, prints some basic metadata for each image in the project and downloads these images at `download_path` if provided.

<<< @/code-snippets/python-client/get_images.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/get_images.py). To run it, the command should be like

```bash
python get_images.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_project 42 --download_path /tmp
```

## Get terms

This script fetches all terms in (the ontology of) a project with ID `id_project` and prints some basic metadata for each term in the project ontology.

<<< @/code-snippets/python-client/get_terms.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/get_terms.py). To run it, the command should be like

```bash
python get_terms.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_project 42
```

## Get annotations

This script fetches all annotations in a project with ID `id_project`, prints some basic metadata for each annotation in the project and downloads a crop, a mask and an alpha-mask for each annotation at `download_path` if provided.

<<< @/code-snippets/python-client/get_annotations.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/get_annotations.py). To run it, the command should be like

```bash
python get_annotations.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_project 42 --download_path /tmp
```

## Add annotations

This script adds 2 annotations in an image with ID `id_image_instance` associated to a project with ID `id_project`. These annotations are a point and a bounding box. A property `my_property` with value `10` is associated to the bounding box. If `id_term` is provided, this term is associated to the annotations.

<<< @/code-snippets/python-client/add_annotation.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/add_annotation.py). To run it, the command should be like

```bash
python add_annotation.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_project 42  --id_image 13209 --id_term 176
```

## Add properties

This script adds a property (key-value pair) to a Cytomine resource: a project, an image or an annotation.

<<< @/code-snippets/python-client/add_properties.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/add_properties.py). To run it, the command should be like

```bash
python add_property.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_project 42  --key "PROJECT_STATUS" --value "PENDING"
```

## Calibrate image

This script shows how to calibrate an image. It updates the resolution and/or magnification of an abstract image (image at storage level) based on the ID of one of its instances in a project.
All future instances of the image will thus be affected. Every instance of this image can still have custom calibration and/or magnification values if set from the graphical interface.

<<< @/code-snippets/python-client/calibrate_image.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/calibrate_image.py). To run it, the command should be like

```bash
python calibrate_image.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --id_image 10372 --resolution 0.499 --magnification 20
```

to set a resolution of 0.499µm/px and 20x magnification to the abstract image related to image instance of id 10372.

## Upload image

This script shows how to use the Python client library to upload an image to Cytomine. The image located at `filepath` on your computer is uploaded to your storage and optionally linked with the project having `id_project` ID. This example is detailled on a [dedicated page](/dev-guide/faq/upload-python).

<<< @/code-snippets/python-client/upload_image.py

This script is also available on [Github](https://github.com/cytomine/Cytomine-python-client/tree/main/examples/upload_image.py). To run it, the command should be like

```bash
python upload_image.py --host https://mycytomine.com --public_key AAA --private_key ZZZ --filepath /data/my-image.svs
```
