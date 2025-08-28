"""Module to handle .toml configs."""

from box import Box
from tomli import load

def load_config(config_path : str) -> Box:
    """
    Loads a config file in toml format.

    Args:
        (config_path): The path to the config.

    Returns:
        (Box): Returns a dictionary with the config values.
    """
    with open(config_path, "rb") as f:
        config = load(f)

    return Box(config)
