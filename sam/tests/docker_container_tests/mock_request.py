"""Module to test docker container for the API."""
import json
import httpx


URL = "http://localhost:6000/api/prediction"

mock_requests = [
    {
        "image_id": 27663330,
        "geometry": {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [[[42872, 124110], [43400, 124110], [43400, 123655], 
                                 [42872, 123655], [42872, 124110]]]
            },
            "properties": {}
        },
        "points": []
    },
    {
        "image_id": 27663330,
        "geometry": {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [[[43425, 121705], [45225, 121705], [45225, 120800], 
                                 [43425, 120800], [43425, 121705]]]
            },
            "properties": {}
        },
        "points": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [44471, 121313]
                },
                "properties": {
                    "label": 1
                }
            },
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [44955, 121030]
                },
                "properties": {
                    "label": 1
                }
            },
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [43699, 121411]
                },
                "properties": {
                    "label": 1
                }
            }
        ]
    }
]

responses = []
for i, payload in enumerate(mock_requests):
    response = httpx.post(URL, json = payload, timeout = 30.0)
    print(f"Response for request {i + 1}: {response.status_code}")
    print(response.json())

    responses.append(response.json())

with open("predictions_output.json", "w", encoding = "utf-8") as f:
    json.dump(responses, f, indent = 4)

print("Test done !")
