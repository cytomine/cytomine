from uuid import uuid4


def unique_name_generator() -> str:
    return f"-{uuid4()}"
