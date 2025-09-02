import torch
from fastapi import APIRouter

from app import __version__
from app.schemas.server import HealthResponse

router = APIRouter(tags=["server"])


@router.get("/")
async def health_check() -> HealthResponse:
    return HealthResponse(
        version=__version__,
        status="healthy",
        gpu=torch.cuda.is_available(),
    )
