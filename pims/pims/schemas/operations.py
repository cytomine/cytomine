from pydantic import BaseModel
from typing import List, Optional


class ImageImportResult(BaseModel):
    name: str
    success: bool
    message: Optional[str] = None


class ImageImportSummary(BaseModel):
    total: int
    successful: int
    failed: int
    results: List[ImageImportResult]


class ImportResult(BaseModel):
    uploaded_files: list[str]
    failed_files: list[dict[str, str]]
    skipped_files: list[str]
    project_created: Optional[bool] = None


class ImportResponse(BaseModel):
    image_summary: ImageImportSummary
