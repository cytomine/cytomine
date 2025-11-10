from pydantic import BaseModel, Field
from typing import List, Optional


class ImportResult(BaseModel):
    name: str
    success: bool
    message: Optional[str] = None


class ImportSummary(BaseModel):
    total: int = 0
    successful: int = 0
    failed: int = 0
    results: List[ImportResult] = Field(default_factory=list)


class ImportResponse(BaseModel):
    image_summary: ImportSummary
    annotation_summary: dict[str, ImportSummary]
