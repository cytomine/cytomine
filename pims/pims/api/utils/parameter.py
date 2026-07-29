from fastapi.params import Path as PathParam
from pathvalidate import sanitize_filename as _sanitize_filename

from pims.api.exceptions import NoAppropriateRepresentationProblem, FilepathNotFoundProblem
from pims.files.file import Path

def filepath_parameter(
    filepath: str = PathParam(
        ..., description="The file path, relative to server base path.",
        examples=['123/my-file.ext']
    ),
):
    path = Path.from_filepath(filepath)
    if not path.exists():
        raise FilepathNotFoundProblem(path)
    return path


def imagepath_parameter(
    filepath: str = PathParam(
        ..., description="The file path, relative to server base path.",
        examples=['123/my-file.ext']
    )
):
    path = Path.from_filepath(filepath)
    if not path.exists():
        raise FilepathNotFoundProblem(path)
    if not path.is_single():
        raise NoAppropriateRepresentationProblem(path)
    return path


def sanitize_filename(filename: str, replacement="-"):
    sanitized = _sanitize_filename(filename, replacement_text=replacement)
    bad_chars = [" ", "(", ")", "+", "*", "/", "@", "'", '"',
                 '$', '€', '£', '°', '`', '[', ']', '#', '?']
    return "".join(c if c not in bad_chars else replacement for c in sanitized)
