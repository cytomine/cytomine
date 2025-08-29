import torch
from fastapi import APIRouter

from app import __version__
from app.schemas.server import HealthResponse

router = APIRouter()


@router.get("/", response_model=HealthResponse, tags=["server"])
async def health_check() -> dict:
    return HealthResponse(
        version=__version__,
        status="healthy",
        gpu=torch.cuda.is_available(),
    )
