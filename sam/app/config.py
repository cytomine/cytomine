"""Environment parameters"""

import torch
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Configurable settings."""

    API_BASE_PATH: str = "/api"

    ANNOTATION_MAX_SIZE: int = 8000

    # Deep learning model
    CHECKPOINT: str = "./weights/weights.pt"
    CONFIG: str = "./configs/sam2.1/sam2.1_hiera_b+.yaml"
    DEVICE: torch.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # Cytomine client
    CYTOMINE_HOST: str
    CYTOMINE_PUBLIC_KEY: str
    CYTOMINE_PRIVATE_KEY: str


def get_settings() -> Settings:
    """
    Get the settings.

    Returns:
        (Settings): The environment settings.
    """
    return Settings()
