package be.cytomine.common.repository.model.tagdomainassociation.payload;

import java.util.Optional;

public record UpdateTagDomainAssociation(
    Optional<Long> tagId,
    Optional<String> domainClassName,
    Optional<Long> domainId
) {}
