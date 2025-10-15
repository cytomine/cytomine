from pydantic import BaseModel
from typing import List, Optional


class ImportResult(BaseModel):
    name: str
    success: bool
    message: Optional[str] = None


class ImportSummary(BaseModel):
    total: int
    successful: int
    failed: int
    results: List[ImportResult]


class ImportResponse(BaseModel):
    image_summary: ImportSummary
    annotation_summary: dict[str, ImportSummary]
