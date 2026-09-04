from pydantic import BaseModel, Field


class ImportResult(BaseModel):
    name: str
    success: bool
    message: str | None = None


class ImportSummary(BaseModel):
    total: int = 0
    successful: int = 0
    failed: int = 0
    results: list[ImportResult] = Field(default_factory=list)


class ImportResponse(BaseModel):
    image_summary: ImportSummary
    annotation_summary: dict[str, ImportSummary]


class JobResponse(BaseModel):
    status: str
    path: str
    content: list[str] = Field(default_factory=list)
