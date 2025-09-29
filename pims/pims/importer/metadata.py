from pims.pims.utils.xml import XMLValidator


class MetadataValidator:
    def __init__(self):
        pass

    @staticmethod
    def validate(file_path: str) -> None:
        validator = XMLValidator(file_path)
        validator.validate(file_path)
