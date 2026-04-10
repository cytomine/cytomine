package be.cytomine.common.repository.model.userannotation.payload;

public record CreateUserAnnotation(long userId, long imageId, long sliceId, long projectId,
                                   String wktLocation, double geometryCompression) {
}
