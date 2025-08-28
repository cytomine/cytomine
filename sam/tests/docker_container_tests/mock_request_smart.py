"""Module to test docker container for the API."""
import json
import httpx


URL = "http://localhost:6000/api/smart_prediction"

mock_requests = [
    {
        "image_id": 27663330,
        "user_id": 563220523,
        "geometry": {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [[[43425, 121705], [45225, 121705], [45225, 120800], 
                                 [43425, 120800], [43425, 121705]]]
            },
            "properties": {}
        }
    },
    {
        "image_id": 27663330,
        "user_id": 563220523,
        "geometry": {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [[[44370, 119680], [47430, 119680], [47430, 116780], 
                                 [44370, 116780], [44370, 119680]]]
            },
            "properties": {}
        }
    }
]

responses = []
for i, payload in enumerate(mock_requests):
    response = httpx.post(URL, json = payload, timeout = 30.0)
    print(f"Response for request {i + 1}: {response.status_code}")
    print(response.json())

    responses.append(response.json())

with open("predictions_output_smart.json", "w", encoding = "utf-8") as f:
    json.dump(responses, f, indent = 4)

print("Test done !")
