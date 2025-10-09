from pydantic import BaseModel


class ApiCredentials(BaseModel):
    public_key: str
    token: str
    signature: str


class CytomineAuth(BaseModel):
    host: str
    public_key: str
    private_key: str
