"""Module to test docker container for the API."""
import json
import httpx


URL = "http://localhost:6000/api/autonomous_prediction"

mock_requests = [
    {
        "annotation_id": 566188354
    }
]

responses = []
for i, payload in enumerate(mock_requests):
    response = httpx.post(
        URL,
        params = payload,
        timeout = 30.0
    )

    print(f"Response for request {i + 1}: {response.status_code}")
    print(response.json())

    responses.append(response.json())

with open("predictions_output_autonomous.json", "w", encoding = "utf-8") as f:
    json.dump(responses, f, indent = 4)

print("Test done !")
