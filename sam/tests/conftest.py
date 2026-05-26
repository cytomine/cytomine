from typing import Generator
from unittest.mock import MagicMock, patch

import pytest
from fastapi.testclient import TestClient

from app.main import app


@pytest.fixture(scope="module")
def client() -> Generator[TestClient, None, None]:
    with patch("app.main.load_predictor", return_value=MagicMock()):
        with TestClient(app) as c:
            yield c
