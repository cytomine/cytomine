import pytest

from pims.api.exceptions import BadRequestException
from pims.utils.iterables import check_array_size, ensure_list
from tests.conftest import not_raises


def test_check_array_size():
    with not_raises(BadRequestException):
        check_array_size([1], [1, 2], True)
        check_array_size(None, [1, 2], True)

    with pytest.raises(BadRequestException):
        check_array_size([1], [2], True)

    with pytest.raises(BadRequestException):
        check_array_size(None, [1], False)

    with pytest.raises(BadRequestException):
        check_array_size([1], [], True)


def test_ensure_list():
    assert ensure_list(3) == [3]
    assert ensure_list((2, 4)) == [(2, 4)]
    assert ensure_list("a") == ['a']
    assert ensure_list([2]) == [2]
    assert ensure_list(None) == []
