package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.TagDomainAssociationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.TagDomainAssociationCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final ACLService aclService;
    private final TagDomainAssociationRepository repository;
    private final TagDomainAssociationCommandService service;
    private final CurrentUserService currentUserService;

    @Override
    public Page<TagDomainAssociationResponse> readAll(Pageable pageable) {
        long userId = currentUserService.getCurrentUserId();
        if (aclService.isAdmin(userId)) {
            return repository.findAllByDeletedNull(pageable).map(service::mapToResponse);
        }
        return repository.findAllReadableByUser(userId, pageable).map(service::mapToResponse);
    }

    @Override
    public Page<TagDomainAssociationResponse> readAllByDomain(
        @PathVariable String domainClassName,
        @PathVariable long domainId,
        Pageable pageable
    ) {
        if (!aclService.canReadDomain(currentUserService.getCurrentUserId(), domainId, domainClassName)) {
            return Page.empty();
        }
        return repository
            .findAllByDomainClassNameAndDomainIdAndDeletedNull(domainClassName, domainId, pageable)
            .map(service::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(
        @RequestBody CreateTagDomainAssociation payload
    ) {
        return service.create(
            currentUserService.getCurrentUserId(), payload,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Override
    public Optional<TagDomainAssociationResponse> read(@PathVariable long id) {
        return repository.findByIdAndDeletedNull(id)
            .filter(e -> aclService.canReadDomain(currentUserService.getCurrentUserId(), e.getDomainId(),
                e.getDomainClassName()))
            .map(service::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestBody UpdateTagDomainAssociation payload
    ) {
        return service.update(
            currentUserService.getCurrentUserId(), id, payload,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Override
    public Optional<HttpCommandResponse> delete(@PathVariable long id) {
        return service.delete(
            currentUserService.getCurrentUserId(), id,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }
}
