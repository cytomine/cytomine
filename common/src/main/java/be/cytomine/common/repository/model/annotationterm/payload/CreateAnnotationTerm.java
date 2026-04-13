package be.cytomine.common.repository.model.annotationterm.payload;

public record CreateAnnotationTerm(long userAnnotationId, long termId, long userId) {
}
