package be.cytomine.common.repository.model.command.payload.request;

import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record UserCommandPayload(long id,
                                 String username,
                                 String email,
                                 Optional<String> lastname,
                                 Optional<String> firstname,
                                 Optional<String> language

) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
