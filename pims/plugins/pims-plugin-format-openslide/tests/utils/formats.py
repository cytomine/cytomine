


def info_test(client, image, slug):
	response = client.get(f'/image/upload_test_{slug}/{image}/info/image')
	assert response.status_code == 200
	assert slug in response.json()['original_format'].lower()
	
def thumb_test(client, image, slug):
	response = client.get(f"/image/upload_test_{slug}/{image}/thumb", headers={"accept": "image/jpeg"})
	assert response.status_code == 200
	
def resized_test(client, image, slug):
	response = client.get(f"/image/upload_test_{slug}/{image}/resized", headers={"accept": "image/jpeg"})
	assert response.status_code == 200
	
def mask_test(client, image, slug):
	response = client.post(f"/image/upload_test_{slug}/{image}/annotation/mask", headers={"accept": "image/jpeg"}, json={"annotations":[{"geometry": "POINT(10 10)"}], "height":50, "width":50})
	assert response.status_code == 200
	
def crop_test(client, image, slug):
	response = client.post(f"/image/upload_test_{slug}/{image}/annotation/crop", headers={"accept": "image/jpeg"}, json={"annotations":[{"geometry": "POINT(10 10)"}], "height":50, "width":50})
	assert response.status_code == 200
	
def crop_null_annot_test(client, image, slug):
	response = client.post(f"/image/upload_test_{slug}/{image}/annotation/crop", headers={"accept": "image/jpeg"}, json={"annotations": [], "height":50, "width":50})
	print(response.__dict__)
	assert response.status_code == 400

def histogram_perimage_test(client, image, slug):
	response = client.get(f"/image/upload_test_{slug}/{image}/histogram/per-image", headers={"accept": "image/jpeg"})
	assert response.status_code == 200
