package org.cytomine.repository.http;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TagDomainAssociationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.common.repository.model.tagdomainassociation.payload.UpdateTagDomainAssociation;

import static be.cytomine.common.repository.http.TagDomainAssociationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TagDomainAssociationController implements TagDomainAssociationHttpContract {
    @Override public Optional<HttpCommandResponse> create(long userId, CreateTagDomainAssociation payload) {
        return Optional.empty();
    }

    @Override public Optional<TagDomainAssociationResponse> read(long id, long userId) {
        return Optional.empty();
    }

    @Override public Optional<HttpCommandResponse> update(long id, long userId, UpdateTagDomainAssociation payload) {
        return Optional.empty();
    }

    @Override public Optional<HttpCommandResponse> delete(long id, long userId) {
        return Optional.empty();
    }
}
