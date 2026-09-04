import pytest

from pims.api.exceptions import NoAcceptableResponseMimetypeProblem
from pims.api.utils.mimetype import (
    OutputExtension, PROCESSING_MIMETYPES, VISUALISATION_MIMETYPES,
    get_output_format
)


def test_get_output_format_simple(app):
    format, mimetype = get_output_format(
        OutputExtension.NONE, 'image/jpeg', VISUALISATION_MIMETYPES
    )
    assert format == OutputExtension.JPEG
    assert mimetype == "image/jpeg"


def test_get_output_format_complex(app):
    accept_header = 'application/signed-exchange;v=b3;q=0.9,text/html,application/xhtml+xml,' \
                    'image/webp,image/apng,application/xml;q=0.9,*/*;q=0.8'

    format, mimetype = get_output_format(
        OutputExtension.NONE, accept_header, VISUALISATION_MIMETYPES
    )
    assert format == OutputExtension.WEBP
    assert mimetype == "image/webp"

    format, mimetype = get_output_format(OutputExtension.NONE, accept_header, PROCESSING_MIMETYPES)
    assert format == OutputExtension.PNG
    assert mimetype == "image/apng"


def test_get_output_format_accept_all(app):
    accept_header = 'image/*'
    format, mimetype = get_output_format(
        OutputExtension.NONE, accept_header, VISUALISATION_MIMETYPES
    )
    assert format == OutputExtension.WEBP
    assert mimetype == "image/webp"


def test_get_output_format_invalid(app):
    accept_header = 'application/json'
    with pytest.raises(NoAcceptableResponseMimetypeProblem):
        get_output_format(OutputExtension.NONE, accept_header, VISUALISATION_MIMETYPES)
