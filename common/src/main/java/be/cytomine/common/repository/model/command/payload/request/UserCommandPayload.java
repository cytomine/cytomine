package be.cytomine.common.repository.model.command.payload.request;

import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;

public record UserCommandPayload(long id, String username, String email, Optional<String> name,
                                 Optional<String> lastname, Optional<String> firstname, Optional<String> language,
                                 boolean developer, Optional<String> origin,Optional<String> privateKey,
                                 Optional<String> publicKey, Set<RoleResponse> roles

) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
