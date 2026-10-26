package be.cytomine.controller.utils;

import java.util.List;

import be.cytomine.common.repository.model.list.filtered.ProjectListAvailableFiltersResponse;

public record CollectionResponse<T>(
        List<T> collection,
        long offset,
        int perPage,
        long size,
        int totalPages
) {}
