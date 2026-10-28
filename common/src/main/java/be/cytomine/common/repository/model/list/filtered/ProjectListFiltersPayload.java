package be.cytomine.common.repository.model.list.filtered;

import java.util.Optional;
import java.util.Set;

public record ProjectListFiltersPayload(Set<String> ontologyNames,
                                        Set<String> roleNames,
                                        Set<String> tagLabels,
                                        Optional<String> name,
                                        Optional<OptionallyBoundedFilter> memberCount,
                                        Optional<OptionallyBoundedFilter> imageCount,
                                        Optional<OptionallyBoundedFilter> manualAnnotationCount,
                                        Optional<OptionallyBoundedFilter> reviewedAnnotationCount) {
    public ProjectListFiltersPayload {
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

    public static ProjectListFiltersPayload empty() {
        return new ProjectListFiltersPayload(Set.of(), Set.of(), Set.of(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());
    }
}
