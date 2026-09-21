from pims.importer.metadata_projects import fetch_existing_project_memberships, merge_project_memberships


def test_merge_appends_new_project_name():
    assert merge_project_memberships(["Project A", "Project B"], "Project C") == [
        "Project A",
        "Project B",
        "Project C",
    ]


def test_merge_never_duplicates_and_keeps_order():
    assert merge_project_memberships(["Project A", "Project B"], "Project A") == [
        "Project A",
        "Project B",
    ]


def test_merge_handles_missing_or_empty_existing_values():
    assert merge_project_memberships(None, "Project A") == ["Project A"]
    assert merge_project_memberships([], "Project A") == ["Project A"]


def test_fetch_existing_project_memberships_maps_hits():
    class FakeIndex:
        def __init__(self, hits):
            self.hits = hits

        def search(self, query, opt_params):
            assert "image.abstract_image_id IN [10, 11]" in opt_params["filter"]
            assert "image.projects" in opt_params["attributesToRetrieve"]
            return {"hits": self.hits}

    index = FakeIndex(
        [
            {"image": {"abstract_image_id": 10, "projects": ["Project A", "Project B"]}},
            {"image": {"abstract_image_id": 11, "projects": None}},
            {"image": {}},
        ]
    )

    memberships = fetch_existing_project_memberships(index, {10, 11})

    assert memberships == {10: ["Project A", "Project B"], 11: []}


def test_fetch_existing_project_memberships_chunks_large_batches():
    class FakeIndex:
        def __init__(self):
            self.filters = []

        def search(self, query, opt_params):
            self.filters.append(opt_params["filter"][0])
            return {"hits": []}

    index = FakeIndex()

    fetch_existing_project_memberships(index, range(2500))

    assert index.filters[0].startswith("image.abstract_image_id IN [0, 1, ")
    assert len(index.filters) == 3
    assert "IN [1000, 1001" in index.filters[1]
    assert "IN [2000, 2001" in index.filters[2]