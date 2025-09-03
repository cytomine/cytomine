from pydantic import BaseModel


class HealthResponse(BaseModel):
    version: str
    status: str
    gpu: bool
