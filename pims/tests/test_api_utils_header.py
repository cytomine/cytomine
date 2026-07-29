from pims.api.utils.header import add_image_size_limit_header, serialize_header


def test_serialize_header():
    assert serialize_header(5) == str(5)
    assert serialize_header([1, 2]) == '1,2'
    assert serialize_header(dict(a=2, b='c'), explode=True) == 'a=2,b=c'
    assert serialize_header(dict(a=2, b='c'), explode=False) == 'a,2,b,c'


def test_add_image_size_limit_header():
    assert len(add_image_size_limit_header(dict(), 100, 100, 100, 100)) == 0
    assert len(add_image_size_limit_header(dict(), 100, 100, 50, 50)) == 1
