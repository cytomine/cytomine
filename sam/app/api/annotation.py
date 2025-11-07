"""Prediction API"""

from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Request
from fastapi.responses import JSONResponse

from app.annotations.segmentation import run_segmentation_pipeline
from app.config import Settings, get_settings
from app.utils.annotations import (
    fetch_included_annotations,
    get_annotation_by_id,
    get_bbox_from_annotation,
    is_invalid_annotation,
    update_annotation_location,
)

router = APIRouter()


@router.post("/autonomous_prediction")
async def autonomous_predict(
    request: Request,
    annotation_id: int,
    settings: Annotated[Settings, Depends(get_settings)],
) -> JSONResponse:
    """
    Function to handle the segmentation request, but here the API only needs the
    'annotation_id' and handles by itself the direct modification of the annotation.

    Args:
        (request: Request): the HTTP request.
        (annotation_id: int): the annotation id.
        (settings: Settings): the settings.

    Returns:
        (JSONResponse): the JSON response containing the HTTP code.
    """
    # Check prompt coordinates format
    try:
        annotation = get_annotation_by_id(annotation_id, settings)

        if is_invalid_annotation(annotation):
            raise HTTPException(
                status_code=400,
                detail=(
                    "The annotation can not be a Point, LineString, LinearRing, "
                    "or have perimeter/area = 0.0 !"
                ),
            )

        user_id = annotation.user
        image_id = annotation.image

        bbox = get_bbox_from_annotation(annotation.location)

        points = fetch_included_annotations(
            image_id=image_id,
            user_id=user_id,
            box_=bbox,
            settings=settings,
        )

        result = run_segmentation_pipeline(
            request=request,
            image_id=image_id,
            geometry={"box": bbox},
            points=points if points else None,
            settings=settings,
            is_shapely_box=True,
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e)) from e

    if not result:
        return JSONResponse(status_code=204, content={"message": "No geometry found"})

    try:
        is_update_ok = update_annotation_location(
            annotation_id=annotation_id, new_location=result, settings=settings
        )

        if is_update_ok is False:
            raise HTTPException(
                status_code=400,
                detail="Failed to update annotation location.",
            )
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e)) from e

    return JSONResponse(
        status_code=200,
        content={"message": "Change completed successfully."},
    )
