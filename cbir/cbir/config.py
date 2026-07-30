"""Environment parameters"""

import torch
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Configurable settings."""

    # App
    api_base_path: str = "/api"

    # Faiss index
    filename: str = "db"
    data_path: str = "/data"

    # Database
    host: str = "localhost"
    port: int = 6379
    db: int = 0

    # Deep learning model
    device: torch.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    extractor: str = "resnet"
    weights: str = "/app/weights/resnet"


def get_settings() -> Settings:
    """
    Get the settings.

    Returns:
        (Settings): The environment settings.
    """
    return Settings()
