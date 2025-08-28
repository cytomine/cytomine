"""Environment parameters"""

import torch
from box import Box
from pydantic_settings import BaseSettings

from src.utils.box_config import load_config


class Settings(BaseSettings):
    """Configurable settings."""

    # Deep learning model
    device: torch.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    config: str = "./configs/sam2.1/sam2.1_hiera_b+.yaml"
    checkpoint: str = "./weights/weights.pt"

    api_base_path: str = "/api"

    keys: Box = load_config('./keys.toml')


def get_settings() -> Settings:
    """
    Get the settings.

    Returns:
        (Settings): The environment settings.
    """
    return Settings()
