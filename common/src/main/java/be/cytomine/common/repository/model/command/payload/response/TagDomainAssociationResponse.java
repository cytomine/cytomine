package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record TagDomainAssociationResponse(
    long id,
    long tagId,
    String domainClassName,
    long domainId,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) implements ApplyCommandResponse {

    @Override
    public DataType getDataType() {
        return DataType.TAG_DOMAIN_ASSOCIATION;
    }
}
