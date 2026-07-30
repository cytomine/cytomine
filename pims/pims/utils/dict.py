import collections.abc
from typing import Any


def get_first(d: dict, keys: list[Any], default: Any = None) -> Any:
    """
    Get first non-null value for the list of keys.
    If all values are null, `default` is returned.
    """
    for k in keys:
        v = d.get(k)
        if v is not None:
            return v
    return default


def invert(d: dict) -> dict:
    """Invert keys and values in a dictionary"""
    return {v: k for k, v in d.items()}


def flatten(d: dict | collections.abc.MutableMapping, parent_key="", sep=".") -> dict:
    """
    Deeply flatten a dictionary.
    Nested dictionary keys are renamed as <parent_key><sep><nested_key>
    """
    items = []
    for k, v in d.items():
        if parent_key:
            if k.startswith("["):
                new_key = parent_key + k
            else:
                new_key = parent_key + sep + k
        else:
            new_key = k
        if isinstance(v, collections.abc.MutableMapping):
            items.extend(flatten(v, new_key, sep=sep).items())
        else:
            items.append((new_key, v))
    return dict(items)
