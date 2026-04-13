package be.cytomine.common.repository.model.userannotation.payload;

import java.util.Optional;

public record UpdateUserAnnotation(Optional<String> wktLocation, Optional<Double> geometryCompression) {
}
