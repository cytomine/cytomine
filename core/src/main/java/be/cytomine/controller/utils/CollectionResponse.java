package be.cytomine.controller.utils;

import java.util.List;

public record CollectionResponse<T>(
        List<T> collection,
        long offset,
        int perPage,
        long size,
        int totalPages
) {}
