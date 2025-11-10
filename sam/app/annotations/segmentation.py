from typing import Any, Dict, Union

import geojson
import numpy as np
from cytomine import Cytomine
from cytomine.models import ImageInstance
from fastapi import HTTPException, Request

from app.config import Settings, get_settings
from app.utils.align_prompts import align_box_prompt
from app.utils.convert_geojson import mask_to_geojson
from app.utils.extract_img import get_roi_around_annotation
from app.utils.postprocess import post_process_segmentation_mask
from app.utils.window import load_cytomine_window_image

ANNOTATION_MAX_SIZE = get_settings().ANNOTATION_MAX_SIZE


def run_segmentation_pipeline(
    request: Request,
    image_id: int,
    geometry: Dict[str, Any],
    settings: Settings,
) -> Union[geojson.Feature, None]:
    """
    Function to run the segmentation model for the incoming request and its prompts.

    Args:
        (request: Request): the HTTP request.
        (image_id: int): the id of the image to segment.
        (geometry: Dict[str, Any]): the box geometry for this image.
        (settings: Settings): the settings.

    Returns:
        (geojson.Feature, or None): Returns the structure as a GeoJSON.
    """
    box_prompt = np.array(geometry["box"].bounds, dtype=np.int32)

    with Cytomine(
        settings.CYTOMINE_HOST,
        settings.CYTOMINE_PUBLIC_KEY,
        settings.CYTOMINE_PRIVATE_KEY,
        verbose=False,
    ):
        img = ImageInstance().fetch(image_id)
        img_height = img.height

        x, y, annot_width, annot_height = get_roi_around_annotation(img, box_prompt)

        scale_x = 1.0
        scale_y = 1.0
        max_size = None

        if annot_width > ANNOTATION_MAX_SIZE:  # annot_width == annot_height
            # if a dim is greater than 20000, img.window might return an error
            # handle this situation by constraining the max_size to ANNOTATION_MAX_SIZE
            scale = annot_width / ANNOTATION_MAX_SIZE

            scale_x /= scale
            scale_y /= scale

            max_size = ANNOTATION_MAX_SIZE

        cropped_img = load_cytomine_window_image(
            img,
            x,
            y,
            annot_width,
            annot_height,
            max_size,
        )
        if cropped_img is None:
            raise HTTPException(
                status_code=500,
                detail="Failed to load image from Cytomine.",
            )

    box_prompt = align_box_prompt(box_prompt, x, y, img_height, scale_x, scale_y)

    predictor = request.app.state.predictor
    predictor.set_image(cropped_img)

    masks, ious, _ = predictor.predict(
        point_coords=None,
        point_labels=None,
        box=box_prompt,
        mask_input=None,
        multimask_output=True,
        return_logits=False,
        normalize_coords=True,
    )

    best_mask = masks[np.argmax(ious)]  # shape: H x W
    output_mask = post_process_segmentation_mask(best_mask)

    if max_size is None:
        geojson_mask = mask_to_geojson(output_mask, img_height, x, y)
    else:
        geojson_mask = mask_to_geojson(
            mask=output_mask,
            image_height=img_height,
            offset_x=x,
            offset_y=y,
            scale_x=scale_x,
            scale_y=scale_y,
        )

    return geojson_mask
