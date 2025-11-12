import io
from typing import Annotated

import numpy as np
from fastapi import APIRouter, File, Form, HTTPException, Request, UploadFile
from fastapi.responses import JSONResponse
from PIL import Image

from app.annotations.segmentation import run_segmentation_pipeline
from app.utils.annotations import get_bbox_from_annotation, has_positive_area

router = APIRouter(prefix="/annotations")


@router.post("/refine")
async def refine(
    request: Request,
    annotation_crop: Annotated[UploadFile, File(...)],
    image_height: Annotated[int, Form(..., gt=0)],
    image_width: Annotated[int, Form(..., gt=0)],
    location: Annotated[str, Form(...)],
) -> JSONResponse:
    if not has_positive_area(location):
        raise HTTPException(
            status_code=400,
            detail="Annotation must have a positive area",
        )

    predictor = request.app.state.predictor

    try:
        bbox = get_bbox_from_annotation(location)

        crop = await annotation_crop.read()
        image = Image.open(io.BytesIO(crop)).convert("RGB")
        crop_array = np.array(image)

        geometry = run_segmentation_pipeline(
            predictor=predictor,
            crop_array=crop_array,
            image_height=image_height,
            image_width=image_width,
            box=bbox,
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e)) from e

    if not geometry:
        return JSONResponse(status_code=204, content={"message": "No geometry found"})

    return JSONResponse(status_code=200, content=geometry)
