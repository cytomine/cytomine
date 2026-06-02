package be.cytomine.common.repository.model.reviewedannotation.payload;

import java.util.List;

public record CreateReviewedAnnotation(long userId, long reviewUserId, long imageId, long sliceId, long projectId,
                                       long parentIdent, String parentClassName, int status,
                                       String wktLocation, double geometryCompression,
                                       List<Long> termIds) {
}
