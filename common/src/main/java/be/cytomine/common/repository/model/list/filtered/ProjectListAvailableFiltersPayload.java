package be.cytomine.common.repository.model.list.filtered;

import java.util.Optional;
import java.util.Set;

public record ProjectListAvailableFiltersPayload(Set<String> ontologyNames,
                                                 Set<String> roleNames,
                                                 Set<String> tagLabels,
                                                 Optional<BoundedFilter> memberCount,
                                                 Optional<BoundedFilter> imageCount,
                                                 Optional<BoundedFilter> manualAnnotationCount,
                                                 Optional<BoundedFilter> reviewedAnnotationCount) {
    public ProjectListAvailableFiltersPayload {
        if (ontologyNames == null) {
            ontologyNames = Set.of();
        }
        if (roleNames == null) {
            roleNames = Set.of();
        }
        if (tagLabels == null) {
            tagLabels = Set.of();
        }
    }

    public static ProjectListAvailableFiltersPayload empty() {
        return new ProjectListAvailableFiltersPayload(Set.of(), Set.of(), Set.of(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }
}
