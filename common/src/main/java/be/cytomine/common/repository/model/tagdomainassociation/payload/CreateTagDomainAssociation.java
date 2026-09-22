package be.cytomine.common.repository.model.tagdomainassociation.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record CreateTagDomainAssociation(
    @JsonAlias("tag") long tagId,
    @NotBlank String domainClassName,
    @JsonAlias("domainIdent") long domainId
) {}
