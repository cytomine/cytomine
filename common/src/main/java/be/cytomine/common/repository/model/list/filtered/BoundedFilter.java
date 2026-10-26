package be.cytomine.common.repository.model.list.filtered;

import java.util.Optional;

public record BoundedFilter(Optional<Long> min, Optional<Long> max) {
}
