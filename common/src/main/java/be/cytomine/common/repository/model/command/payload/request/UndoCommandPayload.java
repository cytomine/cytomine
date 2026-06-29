package be.cytomine.common.repository.model.command.payload.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record UndoCommandPayload(long id, UUID commandId) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
