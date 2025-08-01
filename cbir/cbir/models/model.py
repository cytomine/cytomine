#  Copyright 2023 Cytomine ULiège
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

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
