"""Health Check Module"""

from fastapi import APIRouter
from src import __version__

router = APIRouter()

@router.get("/", tags = ["Health"])
@router.get("/health", tags = ["Health"])
async def health_check() -> dict:
    """Function to check the health of the API."""
    return {
        "message": "This is a health check message.",
        "status": "ok",
        "version": __version__,
    }
