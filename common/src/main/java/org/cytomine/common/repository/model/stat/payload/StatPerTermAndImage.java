package org.cytomine.common.repository.model.stat.payload;

public record StatPerTermAndImage(long imageId, long termId, long countAnnotations) {}
