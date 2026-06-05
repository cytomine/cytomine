package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.TermMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

@Component
@AllArgsConstructor
@Getter
public class TermCommandService
    implements CRUDCommandService<CreateTerm, UpdateTerm, TermCommandPayload, TermEntity, TermResponse> {

    private final TermRepository termRepository;
    private final TermRelationRepository termRelationRepository;
    private final TermMapper termMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Override
    public TermEntity update(TermEntity entity, UpdateTerm payload, Timestamp now) {
        return termMapper.update(entity, payload.name().orElse(entity.getName()),
            payload.color().orElse(entity.getColor()), now);
    }

    @Override
    public TermEntity updateWithPayload(TermEntity entity, TermCommandPayload payload, Timestamp now) {
        return termMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public TermResponse mapToResponse(TermEntity entity) {
        return termMapper.mapToTermResponse(entity);
    }

    @Override
    public TermEntity mapCreateToEntity(CreateTerm createPayload, long userId, Timestamp creationDate) {
        return termMapper.map(createPayload, creationDate);
    }

    @Override
    public TermCommandPayload map(TermEntity entity) {
        return termMapper.mapToTermCommandPayload(entity);
    }

    @Override
    public TermEntity save(TermEntity entity) {
        return termRepository.save(entity);
    }

    @Override
    public UpdateCommandRequest<TermCommandPayload> mapUpdateCommand(long userId, TermCommandPayload before,
        TermCommandPayload after) {

        return new UpdateTermCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<TermCommandPayload> mapCreateCommand(long userId, TermCommandPayload after) {
        return new CreateTermCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<TermCommandPayload> mapDeleteCommand(long userId, TermCommandPayload before) {
        return new DeleteTermCommand(before, userId);
    }

    @Override
    public Optional<TermEntity> get(long id) {
        return termRepository.findById(id);
    }


    @Override
    public boolean canWrite(long userId, long id) {
        return termRepository.findById(id).map(TermEntity::getOntologyId)
            .map(ontologyId -> aclService.canWriteOntology(userId, ontologyId)).orElse(false);
    }

    @Override
    public boolean canDelete(long userId, long id) {
        return termRepository.findById(id).map(TermEntity::getOntologyId)
            .map(ontologyId -> aclService.canDeleteOntology(userId, ontologyId)).orElse(false);
    }
}
