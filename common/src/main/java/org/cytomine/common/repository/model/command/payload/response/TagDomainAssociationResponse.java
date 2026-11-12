package org.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.common.repository.model.command.DataType;

public record TagDomainAssociationResponse(
    long id,
    long tagId,
    String domainClassName,
    long domainId,
    String tagName,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements ApplyCommandResponse {

    @Override
    public DataType getDataType() {
        return DataType.TAG_DOMAIN_ASSOCIATION;
    }
}
