from pydantic import BaseModel
from typing import List, Optional, Dict, Any


class SegmentationRequest(BaseModel):
    """
    Class to represent the request for segmentation.
    Example:
        {
            "image_id": 42,
            "geometry": {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[10, 10], [20, 10], [20, 20], [10, 20], [10, 10]]]
                },
                "properties": {}
            },
            "points": [
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [15, 15]
                    },
                    "properties": {
                        "label": 1
                    }
                }
            ]
        }
    """
    image_id: int
    geometry: Dict[str, Any]
    points: Optional[List[Dict[str, Any]]] = None  # List of GeoJSON Feature that correspond to points.


class SmartSegmentationRequest(BaseModel):
    """
    Class to represent the request for segmentation, but here the core does not need to provide the point prompts,
    the point prompts are inferred from the point annotations of the user that are located inside the geometry (Box),
    on the same WSI.
    Example:
        {
            "image_id": 42,
            "user_id" : 1,
            "geometry": {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[10, 10], [20, 10], [20, 20], [10, 20], [10, 10]]]
                },
                "properties": {}
            }
        }
    """
    image_id: int
    user_id: int
    geometry: Dict[str, Any]
