"""Deep learning base model"""

from abc import ABCMeta

import torch
from torch import nn


class Model(nn.Module, metaclass=ABCMeta):
    """Base model"""

    def __init__(
        self,
        n_features: int = 128,
        device: torch.device = torch.device("cpu"),
    ) -> None:
        super().__init__()

        self.n_features = n_features
        self.device = device
