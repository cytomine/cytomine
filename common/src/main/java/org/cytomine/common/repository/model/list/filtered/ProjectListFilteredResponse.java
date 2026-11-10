package org.cytomine.common.repository.model.list.filtered;

import org.springframework.data.domain.Page;

import org.cytomine.common.repository.model.command.payload.response.ProjectListRowResponse;

public record ProjectListFilteredResponse(
    Page<ProjectListRowResponse> content,
    ProjectListAvailableFiltersPayload availableFilters
) {
}
