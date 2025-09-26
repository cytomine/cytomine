from fastapi import Query

from pims.importer.dataset import DatasetImporter


def get_dataset_importer(
    storage_id: int = Query(
        ...,
        description="The storage where to import the datasets",
    ),
    dataset_names: str = Query(
        None,
        description="Comma-separated list of dataset names to import",
    ),
) -> DatasetImporter:
    return DatasetImporter(storage_id, dataset_names)
