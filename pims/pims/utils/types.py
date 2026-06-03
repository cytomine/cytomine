import json
from datetime import datetime
from typing import Any, Union


def parse_json(value: Any, raise_exc: bool = False) -> Union[dict, None]:
    try:
        return json.loads(value)
    except:  # noqa
        if raise_exc:
            raise
        return None


def parse_boolean(value: Any, raise_exc: bool = False) -> Union[bool, None]:
    _true_set = {"yes", "true", "t", "y", "1"}
    _false_set = {"no", "false", "f", "n", "0"}

    if value is True or value is False:
        return value
    elif isinstance(value, str):
        value = value.lower()
        if value in _true_set:
            return True
        if value in _false_set:
            return False

    if raise_exc:
        raise ValueError('Expected "%s"' % '", "'.join(_true_set | _false_set))
    return None


def parse_float(value: Any, raise_exc: bool = False) -> Union[float, None]:
    if isinstance(value, str):
        value = value.replace(",", ".")
    try:
        return float(value)
    except:  # noqa
        if raise_exc:
            raise
        return None


def parse_int(value: Any, raise_exc: bool = False) -> Union[int, None]:
    try:
        return int(value)
    except:  # noqa
        if raise_exc:
            raise
        return None


def parse_datetime(
    value: Any,
    formats: list[str] | None = None,
    raise_exc: bool = False,
) -> Union[datetime, None]:
    if formats is None:
        formats = ["%Y:%m:%d %H:%M:%S", "%m/%d/%y %H:%M:%S"]

    for format in formats:
        try:
            return datetime.strptime(value, format)
        except (ValueError, TypeError):
            continue
    if raise_exc:
        raise ValueError
    return None


def is_int(value: Any) -> bool:
    try:
        int(value)
        return True
    except ValueError:
        return False
