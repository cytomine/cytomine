package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.TermRelationMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.RelationRepository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

@Component
@RequiredArgsConstructor
@Getter
public class TermRelationCommandService implements
    CRUDCommandService<CreateTermRelation, UpdateTermRelation, TermRelationCommandPayload, TermRelationEntity,
        TermRelationResponse> {
    private final TermRepository termRepository;
    private final TermRelationRepository termRelationRepository;
    private final RelationRepository relationRepository;
    private final TermRelationMapper termRelationMapper;
    private final CommandV2Repository commandV2Repository;
    @Setter
     private  ApplyCommandService applyCommandService;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Override
    public TermRelationEntity save(TermRelationEntity entity) {
        return termRelationRepository.save(entity);
    }

    @Override
    public TermRelationCommandPayload map(TermRelationEntity entity) {
        long ontologyId = termRepository.findById(entity.getTerm1Id()).orElseThrow().getOntologyId();
        return termRelationMapper.mapToTermRelationCommandPayload(entity, ontologyId);
    }


    @Override
    public TermRelationEntity updateEntityWithPayload(TermRelationEntity entity,
        TermRelationCommandPayload payload,
        Timestamp now) {
        return termRelationMapper.updateTermRelationWithPayload(entity, payload, now);
    }

    @Override
    public TermRelationResponse mapToResponse(TermRelationEntity entity) {
        return termRelationMapper.mapToTermRelationResponse(entity);
    }

    @Override
    public TermRelationEntity mapCreateToEntity(CreateTermRelation createPayload, long userId, Timestamp creationDate) {
        long parentId = relationRepository.findParent().getId();
        return termRelationMapper.mapToTermRelationEntity(createPayload, creationDate, parentId);
    }

    @Override
    public UpdateCommandRequest<TermRelationCommandPayload> mapUpdateCommand(long userId,
        TermRelationCommandPayload before,
        TermRelationCommandPayload after) {
        return new UpdateTermRelationCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<TermRelationCommandPayload> mapCreateCommand(long userId,
        TermRelationCommandPayload after) {
        return new CreateTermRelationCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<TermRelationCommandPayload> mapDeleteCommand(long userId,
        TermRelationCommandPayload before) {
        return new DeleteTermRelationCommand(before, userId);
    }

    @Override
    public Optional<TermRelationEntity> get(long id) {
        return termRelationRepository.findById(id);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return termRelationRepository.findById(id)
            .map(TermRelationEntity::getTerm1Id)
            .flatMap(termRepository::findById)
            .map(TermEntity::getOntologyId)
            .map(ontologyId -> aclService.canWriteOntology(userId, id))
            .orElse(false);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return termRelationRepository.findById(id)
            .map(TermRelationEntity::getTerm1Id)
            .flatMap(termRepository::findById)
            .map(TermEntity::getOntologyId)
            .map(ontologyId -> aclService.canDeleteOntology(userId, id))
            .orElse(false);
    }

    @Override
    public TermRelationEntity updateEntityWithEntity(TermRelationEntity entity,
        UpdateTermRelation updatePayload,
        Timestamp now) {
        return termRelationMapper.update(entity,
            updatePayload.term1Id().orElse(entity.getTerm1Id()),
            updatePayload.term2Id().orElse(entity.getTerm2Id()),
            now);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.canWriteOntology(userId, id);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.canDeleteOntology(userId, id);
    }

}
