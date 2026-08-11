"""Resnet models"""

import torch
from torch.nn import Linear
from torch.nn.functional import normalize
from torchvision.models import resnet50

from cbir.models.model import Model


class Resnet(Model):
    """Resnet50 model"""

    def __init__(
        self,
        n_features: int = 128,
        device: torch.device = torch.device("cpu"),
    ) -> None:
        super().__init__(n_features, device=device)

        self.model = resnet50()
        self.model.fc = Linear(self.model.fc.in_features, n_features)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """Apply forward pass."""
        inputs = x.to(self.device)

        return normalize(self.model(inputs))
