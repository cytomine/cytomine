package be.cytomine.common.repository.model.list.filtered;

import java.util.Optional;
import java.util.Set;

public record ProjectListAvailableFiltersResponse(Set<String> ontologyNames, Set<String> roleNames,
                                                  Set<String> tagLabels,
                                                  Optional<BoundedFilter> memberCount,
                                                  Optional<BoundedFilter> imageCount,
                                                  Optional<BoundedFilter> manualAnnotationCount,
                                                  Optional<BoundedFilter> reviewedAnnotationCount) {

    public ProjectListAvailableFiltersResponse {
        if (ontologyNames == null) {ontologyNames = Set.of();}
        if (roleNames == null) {roleNames = Set.of();}
        if (tagLabels == null) {tagLabels = Set.of();}
        if (memberCount == null) {memberCount = Optional.empty();}
        if (imageCount == null) {imageCount = Optional.empty();}
        if (manualAnnotationCount == null) {manualAnnotationCount = Optional.empty();}
        if (reviewedAnnotationCount == null) {reviewedAnnotationCount = Optional.empty();}
    }
}
