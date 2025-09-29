from pydantic import BaseModel
from typing import Optional


class ImportResult(BaseModel):
    uploaded_files: list[str]
    failed_files: list[dict[str, str]]
    skipped_files: list[str]
    project_created: Optional[bool] = None


class ImportResponse(BaseModel):
    valid_datasets: dict[str, ImportResult]
    invalid_datasets: list[str]
