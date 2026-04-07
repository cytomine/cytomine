package be.cytomine.common.repository.model.termrelation.payload;

public record CreateTermRelation(long term1Id, long term2Id, long relationType) {
}
