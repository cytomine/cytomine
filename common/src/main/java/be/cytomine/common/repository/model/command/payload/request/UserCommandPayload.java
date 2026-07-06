package be.cytomine.common.repository.model.command.payload.request;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record UserCommandPayload(long id,
                                 String username) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
