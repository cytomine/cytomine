from collections.abc import Iterable


def merge_project_memberships(existing_projects: Iterable[str] | None, project_name: str) -> list[str]:
    """Union-append ``project_name`` to the ``image.projects`` list, preserving order and never duplicating."""
    projects = list(existing_projects) if existing_projects else []
    if project_name not in projects:
        projects.append(project_name)
    return projects


def fetch_existing_project_memberships(index, abstract_image_ids: Iterable[int]) -> dict[int, list[str]]:
    """Fetch the current ``image.projects`` values of every document whose abstract image is in the given ids.

    Used to seed memberships on (re)index, so a full document replacement never wipes project memberships written
    by core lifecycle hooks.
    """
    ids = sorted(set(abstract_image_ids))
    memberships: dict[int, list[str]] = {}
    batch_size = 1000
    for from_ in range(0, len(ids), batch_size):
        chunk = ids[from_: from_ + batch_size]
        in_filter = "image.abstract_image_id IN [" + ", ".join(str(image_id) for image_id in chunk) + "]"
        response = index.search(
            "",
            {
                "filter": [in_filter],
                "attributesToRetrieve": ["image.abstract_image_id", "image.projects"],
            },
        )
        for hit in response.get("hits") or []:
            image = hit.get("image") or {}
            abstract_image_id = image.get("abstract_image_id")
            if abstract_image_id is None:
                continue
            memberships[int(abstract_image_id)] = list(image.get("projects") or [])
    return memberships