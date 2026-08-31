import pytest

from pims.api.utils.range_parameter import is_range, parse_range


def test_is_range():
    assert is_range("10:20") is True
    assert is_range("20:10") is True
    assert is_range(":10") is True
    assert is_range("10:") is True
    assert is_range(":") is True
    assert is_range("a") is False
    assert is_range("2:3:4") is False


def test_parse_range():
    assert parse_range("10:20", 0, 100) == range(10, 20)
    assert parse_range("20:10", 0, 100) == range(10, 20)
    assert parse_range(":10", 0, 100) == range(0, 10)
    assert parse_range("10:", 0, 100) == range(10, 100)
    assert parse_range(":", 0, 100) == range(0, 100)

    with pytest.raises(ValueError):
        parse_range("a", 0, 100)

    with pytest.raises(ValueError):
        parse_range("2:3:4", 0, 100)
