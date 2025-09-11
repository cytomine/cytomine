import torch
from fastapi.testclient import TestClient

from app import __version__


def test_health(client: TestClient) -> None:
    response = client.get("/")
    data = response.json()

    assert response.status_code == 200
    assert data["version"] == __version__
    assert data["status"] == "healthy"
    assert data["gpu"] == torch.cuda.is_available()
