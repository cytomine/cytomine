package be.cytomine.common.repository.model.command.payload.request;

import java.util.Locale;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record UserCommandPayload(long id, String username, String email, Optional<String> name,
                                 Optional<String> lastname, Optional<String> firstname, Optional<Locale> locale

) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
