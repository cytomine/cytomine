package be.cytomine.dto.annotation;

public record AnnotationResponse(long id, long slice_id, long layer_id, byte[] location) {}
