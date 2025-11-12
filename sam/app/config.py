"""Environment parameters"""

import logging
import os
from functools import lru_cache

import torch
from pydantic_settings import BaseSettings, SettingsConfigDict

logger = logging.getLogger("sam.app")


class Settings(BaseSettings):
    """Configurable settings."""

    model_config = SettingsConfigDict(extra="ignore")

    API_BASE_PATH: str = "/api"

    ANNOTATION_MAX_SIZE: int = 8000

    # Deep learning model
    WEIGHTS_PATH: str = "./weights"
    CHECKPOINT: str = "weights.pt"
    CONFIG: str = "./configs/sam2.1/sam2.1_hiera_b+.yaml"
    DEVICE: torch.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


@lru_cache()
def get_settings():
    env_file = os.getenv("CONFIG_FILE", ".env")
    logger.info(f"Loading config from {env_file}")
    return Settings(_env_file=env_file)
