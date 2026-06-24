package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.TagDomainAssociationMapper;
import org.cytomine.repository.persistence.TagDomainAssociationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TagDomainAssociationCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ACLService aclService;
    private final TagDomainAssociationMapper mapper;
    private final TagDomainAssociationRepository repository;
    private final TagDomainAssociationCommandService service;

    @Override
    public Optional<HttpCommandResponse> create(
        @RequestParam long userId,
        @RequestBody CreateTagDomainAssociation payload
    ) {
        return service.create(
            userId, payload,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Override
    public Page<TagDomainAssociationResponse> readAll(@RequestParam long userId, Pageable pageable) {
        if (aclService.isAdmin(userId)) {
            return repository.findAllByDeletedNull(pageable).map(mapper::mapToResponse);
        }
        return repository.findAllReadableByUser(userId, pageable).map(mapper::mapToResponse);
    }

    @Override
    public Optional<TagDomainAssociationResponse> read(@PathVariable long id, @RequestParam long userId) {
        return repository.findByIdAndDeletedNull(id)
            .filter(e -> aclService.canReadDomain(userId, e.getDomainId(), e.getDomainClassName()))
            .map(mapper::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateTagDomainAssociation payload
    ) {
        return service.update(
            userId, id, payload,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Override
    public Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId) {
        return service.delete(
            userId, id,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }
}
