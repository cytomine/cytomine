import os

from pims.files.file import Path

REQUIRED_DIRECTORIES = {
    "IMAGES",
}


def is_already_imported(image_path: Path, data_path: Path) -> bool:
    """Check if an image was already imported."""

    for upload_dir in data_path.iterdir():
        if not upload_dir.is_dir():
            continue
        for candidate in upload_dir.iterdir():
            if candidate.is_symlink() and candidate.resolve() == image_path.resolve():
                return True
    return False


def check_dataset_structure(dataset_root: str) -> tuple[bool, list[str]]:
    """
    Check if the dataset is well structured
    Return (is_valid, missing_directories) for the given dataset path.
    """
    dataset_directory = [entry for entry in os.scandir(dataset_root) if entry.is_dir()]
    if len(dataset_directory) != 1:
        return (
            False,
            [f"Expected exactly 1 UUID directory, found {len(dataset_directory)}"],
        )

    dataset_path = dataset_directory.pop()
    actual_directories = {
        entry.name.upper() for entry in os.scandir(dataset_path) if entry.is_dir()
    }

    missing = [
        required
        for required in REQUIRED_DIRECTORIES
        if required not in actual_directories
    ]

    return len(missing) == 0, missing
