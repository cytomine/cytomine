package be.cytomine.common.repository.model.tagdomainassociation.payload;

import jakarta.validation.constraints.NotBlank;

public record CreateTagDomainAssociation(
    long tagId,
    @NotBlank String domainClassName,
    long domainId
) {}
