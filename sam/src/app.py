"""Segment Anything API"""

from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI

from sam2.build_sam import build_sam2
from sam2.sam2_image_predictor import SAM2ImagePredictor

from src import __version__
from src.api import prediction, health
from src.config import Settings, get_settings


def load_predictor(settings: Settings) -> SAM2ImagePredictor:
    """Load the weights of the model and creates a new predictor instance."""
    sam2_model = build_sam2(settings.config, settings.checkpoint, device = settings.device)

    return SAM2ImagePredictor(sam2_model)


@asynccontextmanager
async def lifespan(local_app: FastAPI) -> AsyncGenerator[None, None]:
    """Lifespan of the app."""

    local_app.state.predictor = load_predictor(get_settings())
    yield

prefix = get_settings().api_base_path

app = FastAPI(
    title = "Cytomine Segment Anything Server",
    description = "Cytomine Segment Anything Server HTTP API.",
    version = __version__,
    lifespan = lifespan,
    license_info = {
        "name": "Apache 2.0",
        "identifier": "Apache-2.0",
        "url": "https://www.apache.org/licenses/LICENSE-2.0.html",
    },
)

app.include_router(router = prediction.router, prefix = prefix)
app.include_router(router = health.router)
