from pydantic import BaseModel


class WktAnnotation(BaseModel):
    wkt: str
    properties: dict[str, str]
