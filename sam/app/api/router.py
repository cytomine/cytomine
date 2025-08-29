from fastapi import APIRouter

from app.api.routes import (
    prediction,
    server,
)

api_router = APIRouter()
api_router.include_router(prediction.router)
api_router.include_router(server.router)
