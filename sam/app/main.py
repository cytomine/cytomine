"""Segment Anything API"""

import os
from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from sam2.build_sam import build_sam2
from sam2.sam2_image_predictor import SAM2ImagePredictor

from app import __version__
from app.api import annotation, server
from app.config import Settings, get_settings
from app.download_weights import download_weights


def load_predictor(settings: Settings) -> SAM2ImagePredictor:
    """Load the weights of the model and creates a new predictor instance."""
    model = build_sam2(
        settings.CONFIG,
        os.path.join(settings.WEIGHTS_PATH, settings.CHECKPOINT),
        device=settings.DEVICE,
    )

    return SAM2ImagePredictor(model)


@asynccontextmanager
async def lifespan(local_app: FastAPI) -> AsyncGenerator[None, None]:
    """Lifespan of the app."""

    local_app.state.predictor = load_predictor(get_settings())
    yield


download_weights()

app = FastAPI(
    title="Cytomine Segment Anything Server",
    description="Cytomine Segment Anything Server HTTP API.",
    version=__version__,
    lifespan=lifespan,
    license_info={
        "name": "Apache 2.0",
        "identifier": "Apache-2.0",
        "url": "https://www.apache.org/licenses/LICENSE-2.0.html",
    },
)
app.include_router(annotation.router, prefix=get_settings().API_BASE_PATH)
app.include_router(server.router)
