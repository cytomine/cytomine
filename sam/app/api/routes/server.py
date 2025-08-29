from fastapi import APIRouter

from app import __version__

router = APIRouter()


@router.get("/", tags=["server"])
async def health_check() -> dict:
    return {
        "message": "This is a health check message.",
        "status": "ok",
        "version": __version__,
    }
