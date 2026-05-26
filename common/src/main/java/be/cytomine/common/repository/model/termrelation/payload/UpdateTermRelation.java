package be.cytomine.common.repository.model.termrelation.payload;

import java.util.Optional;

public record UpdateTermRelation(Optional<Long> term1Id, Optional<Long> term2Id, Optional<Long> relationType) {

}
