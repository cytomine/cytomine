from fastapi import APIRouter
from pydantic import BaseModel, Field

from pims import __version__
from pims.config import ReadableSettings, get_settings

router = APIRouter(prefix=get_settings().api_base_path, tags=["Server"])


class ServerInfo(BaseModel):
    version: str = Field(..., description="PIMS version")
    settings: ReadableSettings


@router.get("/ping")
def ping() -> dict:
    return {"ping": "pong"}


@router.get("/info")
async def show_status() -> ServerInfo:
    """
    PIMS Server status.
    """
    return ServerInfo(version=__version__, settings=get_settings())
