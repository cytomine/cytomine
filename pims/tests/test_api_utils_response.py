import pint

from pims.api.utils.response import convert_quantity, response_list


def test_response_list():
    items = list()
    resp = response_list(items)
    assert resp == dict(items=[], size=0)

    items = ["a", "b"]
    resp = response_list(items)
    assert resp == dict(items=items, size=len(items))


def test_convert_quantity():
    assert convert_quantity(None, 'meters') is None
    assert convert_quantity(3, 'meters') == 3

    ureg = pint.UnitRegistry()
    assert convert_quantity(3 * ureg('cm'), 'meters') == 0.03
