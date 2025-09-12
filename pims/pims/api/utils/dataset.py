import os

REQUIRED_DIRECTORIES = {
    "IMAGES",
}


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
