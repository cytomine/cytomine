import base64
import hashlib
import hmac

from starlette.requests import Request

from pims.api.exceptions import AuthenticationException


def parse_authorization_header(raw_headers):
    auth = raw_headers.get("authorization")
    if auth is None or not auth.startswith("CYTOMINE") \
            or ' ' not in auth or ':' not in auth:
        raise AuthenticationException("Auth failed")

    public_key = auth[(auth.index(" ") + 1):(auth.index(":"))]
    signature = auth[(auth.index(":") + 1):]
    return public_key, signature


def parse_request_token(request: Request):
    headers = request.headers

    md5 = headers.get("content-MD5", "")
    date = headers.get("date", headers.get("dateFull", ""))

    content_type = headers.get(
        "content-type-full",
        headers.get(
            "Content-Type",
            headers.get("content-type", "")
        )
    )
    content_type = "" if content_type == "null" else content_type

    return f"{request.method}\n{md5}\n{content_type}\n{date}"


def sign_token(private_key, token):
    return base64.b64encode(
        hmac.new(
            bytes(private_key, 'utf-8'),
            token.encode('utf-8'),
            hashlib.sha1
        ).digest()
    ).decode('utf-8')
