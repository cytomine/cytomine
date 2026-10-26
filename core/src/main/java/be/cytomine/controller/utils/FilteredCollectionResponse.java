package be.cytomine.controller.utils;

import java.util.List;

public record FilteredCollectionResponse<T,F>(
        List<T> collection,
        long offset,
        int perPage,
        long size,
        int totalPages,
        F filters
) {}
