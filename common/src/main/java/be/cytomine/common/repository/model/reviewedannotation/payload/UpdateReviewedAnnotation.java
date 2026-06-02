package be.cytomine.common.repository.model.reviewedannotation.payload;

import java.util.Optional;

public record UpdateReviewedAnnotation(Optional<String> wktLocation, Optional<Double> geometryCompression) {
}
