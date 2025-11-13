from math import floor
from typing import Tuple, Union

import geojson
import numpy as np
from sam2.sam2_image_predictor import SAM2ImagePredictor
from shapely.geometry import Polygon

from app.config import get_settings
from app.utils.align_prompts import align_box_prompt
from app.utils.annotations import mask_to_polygon
from app.utils.postprocess import post_process_segmentation_mask

ANNOTATION_MAX_SIZE = get_settings().ANNOTATION_MAX_SIZE


def get_roi_around_annotation(
    image_height: int,
    image_width: int,
    box: np.ndarray,
    crop_size: int,
) -> Tuple[int, int]:
    """
    Function to get the position of the annotation to extract with the top left corner,
    width and height.

    With this function, the zoom-out factor has been taken into account.

    Args:
        (img : ImageInstance): the image from which the patch will be extracted.
        (box: np.ndarray): the box prompt in format [x_min, y_min, x_max, y_max].

    Returns:
        Tuple of:
            - (int): x of top left corner.
            - (int): y of top left corner.
    """
    annotation_width = box[2] - box[0]
    annotation_height = box[3] - box[1]
    x = box[0]
    y = box[3]

    h = (crop_size - annotation_width) / 2
    v = (crop_size - annotation_height) / 2
    x = floor(x - h)
    y = floor(image_height - y - v)

    x = min(x, image_width - crop_size)
    y = min(y, image_height - crop_size)
    x = max(0, x)
    y = max(0, y)

    return x, y


def run_segmentation_pipeline(
    predictor: SAM2ImagePredictor,
    crop_array: np.ndarray,
    image_height: int,
    image_width: int,
    box: Polygon,
) -> Union[geojson.Feature, None]:
    """
    Function to run the segmentation model for the incoming request and its prompts.

    Args:
        (request: Request): the HTTP request.
        (box: Polygon): the box geometry for this image.

    Returns:
        (geojson.Feature, or None): Returns the structure as a GeoJSON.
    """
    box_prompt = np.array(box.bounds, dtype=np.int32)

    crop_size = crop_array.shape[0]

    x, y = get_roi_around_annotation(
        image_height,
        image_width,
        box_prompt,
        crop_size,
    )

    scale_x = 1.0
    scale_y = 1.0

    if crop_size > ANNOTATION_MAX_SIZE:  # annot_width == annot_height
        # if a dim is greater than 20000, img.window might return an error
        # handle this situation by constraining the max_size to ANNOTATION_MAX_SIZE
        scale = crop_size / ANNOTATION_MAX_SIZE

        scale_x /= scale
        scale_y /= scale

    box_prompt = align_box_prompt(box_prompt, x, y, image_height, scale_x, scale_y)

    predictor.set_image(crop_array)

    masks, ious, _ = predictor.predict(
        point_coords=None,
        point_labels=None,
        box=box_prompt,
        mask_input=None,
        multimask_output=True,
        return_logits=False,
        normalize_coords=True,
    )

    best_mask = masks[np.argmax(ious)]
    output_mask = post_process_segmentation_mask(best_mask)

    geometry = mask_to_polygon(
        output_mask,
        image_height,
        x,
        y,
        scale_x=scale_x,
        scale_y=scale_y,
    )

    return geometry
