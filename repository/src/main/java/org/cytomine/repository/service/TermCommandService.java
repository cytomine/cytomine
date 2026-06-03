package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

@Component
@AllArgsConstructor
public class TermCommandService implements CRUDCommandService<CreateTerm, UpdateTerm, TermCommandPayload> {

    private final TermRepository termRepository;
    private final TermRelationRepository termRelationRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Override
    @Transactional
    public Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now) {
        return termRepository.findById(id)
            .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId())).map(termEntity -> {
                DeleteTermCommand deleteCommand =
                    new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity), userId,
                        termEntity.getOntologyId());
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termRelationRepository.findAllByTerm1IdOrTerm2Id(id, id).forEach(rel -> rel.setDeleted(Timestamp.valueOf(now)));
                termEntity.setDeleted(Timestamp.valueOf(now));
                return saveAndBuildResponse(termEntity, Commands.DELETE_TERM, commandV2Entity.getId());
            });
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateTerm createTerm, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, createTerm.ontology())) {
            return Optional.empty();
        }

        TermEntity termEntity = ontologyMapper.map(createTerm, now);
        TermEntity savedEntity = termRepository.save(termEntity);
        TermCommandPayload termCommandPayload = ontologyMapper.mapToTermCommandPayload(savedEntity);
        CreateTermCommand insertTermCommand =
            new CreateTermCommand(termCommandPayload, userId, termCommandPayload.ontology());

        CommandV2Entity commandV2Entity =
            commandV2Repository.save(commandMapper.map(insertTermCommand, now, now, userId));

        TermResponse termResponse = ontologyMapper.map(savedEntity);
        return Optional.of(new HttpCommandResponse(true, termResponse, commandV2Entity.getId(), Commands.CREATE_TERM));
    }

    @Override
    @Transactional
    public Optional<HttpCommandResponse> update(long userId, long id, UpdateTerm updateTerm, LocalDateTime now) {
        return termRepository.findById(id).filter(entity -> aclService.canWriteOntology(userId, entity.getOntologyId()))
            .map(termEntity -> {
                TermCommandPayload beforePayload = ontologyMapper.mapToTermCommandPayload(termEntity);
                updateTerm.name().ifPresent(termEntity::setName);
                updateTerm.color().ifPresent(termEntity::setColor);
                TermEntity savedEntity = termRepository.save(termEntity);
                UpdateTermCommand updateCommand =
                    new UpdateTermCommand(id, beforePayload, ontologyMapper.mapToTermCommandPayload(savedEntity),
                        userId, termEntity.getOntologyId());

                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(updateCommand, now, now, userId));

                TermResponse termResponse = ontologyMapper.map(savedEntity);

                return new HttpCommandResponse(true, termResponse, commandV2Entity.getId(), Commands.UPDATE_TERM);
            });
    }

    @Override
    public Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId,
        TermCommandPayload payload, LocalDateTime now) {
        return termRepository.findById(payload.id()).map(entity -> {
            entity.setColor(payload.color());
            entity.setName(payload.name());
            entity.setUpdated(Timestamp.valueOf(now));
            return saveAndBuildResponse(entity, Commands.UPDATE_TERM, commandId);
        });
    }

    @Override
    public Optional<HttpCommandResponse> restore(UUID commandId, long id, String command, LocalDateTime now) {
        return termRepository.findById(id).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(Timestamp.valueOf(now));
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    @Override
    public boolean canWrite(long userId, long id) {
        return aclService.canWriteOntology(userId, id);
    }

    @Override
    public boolean canDelete(long userId, long id) {
        return aclService.canDeleteOntology(userId, id);
    }

    @Override
    public Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, LocalDateTime now) {
        return termRepository.findById(id).map(entity -> {
            entity.setDeleted(Timestamp.valueOf(now));
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    private HttpCommandResponse saveAndBuildResponse(TermEntity entity, String command, UUID commandId) {
        TermEntity saved = termRepository.save(entity);
        TermResponse response = ontologyMapper.map(saved);
        return new HttpCommandResponse(true, response, commandId, command);
    }
}
