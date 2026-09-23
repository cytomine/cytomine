package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.common.repository.model.tagdomainassociation.payload.UpdateTagDomainAssociation;

@HttpExchange(TagDomainAssociationHttpContract.ROOT_PATH)
public interface TagDomainAssociationHttpContract {
    String ROOT_PATH = "/tag-domain-associations";

    @GetExchange
    Page<TagDomainAssociationResponse> readAll(Pageable pageable);

    @GetExchange("/domain/{domainClassName}/{domainId}")
    Page<TagDomainAssociationResponse> readAllByDomain(
        @PathVariable String domainClassName,
        @PathVariable long domainId,
        Pageable pageable
    );

    @PostExchange
    Optional<HttpCommandResponse> create(
        @Valid @RequestBody CreateTagDomainAssociation payload
    );

    @GetExchange("/{id}")
    Optional<TagDomainAssociationResponse> read(@PathVariable long id);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestBody UpdateTagDomainAssociation payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id);
}
