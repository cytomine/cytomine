from pims import __version__


def test_status(app, client):
    response = client.get("/info")
    assert response.status_code == 200

    json = response.json()
    assert json["version"] == __version__
