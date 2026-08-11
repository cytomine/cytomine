def test_disk_usage(app, client, fake_files):
    response = client.get('/disk-usage')
    assert response.status_code == 200


def test_disk_usage_v1(app, client, fake_files):
    response = client.get('/storage/size.json')
    assert response.status_code == 200
