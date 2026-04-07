package org.cytomine.repository.service;

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
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Callback;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

@Component
@AllArgsConstructor
public class TermRelationCommandService {
    private final TermRepository termRepository;
    private final TermRelationRepository termRelationRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> deleteTermRelation(Long id, Long userId, LocalDateTime now) {
        return termRelationRepository.findById(id)
            .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId())).map(termEntity -> {
                DeleteTermRelationCommand deleteCommand =
                    new DeleteTermRelationCommand(id, ontologyMapper.mapToTermRelationCommandPayload(termEntity),
                        userId, termEntity.getOntologyId());
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termEntity.setDeleted(now);
                return saveAndBuildResponse(termEntity, Commands.DELETE_TERM_RELATION, commandV2Entity.getId());
            });
    }

    public Optional<HttpCommandResponse> createTermRelation(Long userId, CreateTermRelation createTerm, LocalDateTime now) {
        return termRepository.findById(createTerm.term1Id())
            .filter(entity -> aclService.canWriteOntology(userId, entity.getOntologyId()))
            .filter(entity -> aclService.canWriteOntology(userId, entity.getTerm2IdOntologyId()))

            .map(termEntity -> {

                TermRelationEntity termEntity = ontologyMapper.mapToTermRelationEntity(createTerm, now);
                TermRelationEntity savedEntity = termRelationRepository.save(termEntity);
                TermRelationCommandPayload termCommandPayload =
                    ontologyMapper.mapToTermRelationCommandPayload(savedEntity);
                CreateTermCommand insertTermCommand =
                    new CreateTermCommand(termCommandPayload, userId, termCommandPayload.ontologyId());

                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(insertTermCommand, now, now, userId));

                TermResponse termResponse = ontologyMapper.map(savedEntity);
                Callback callback = new Callback(Commands.CREATE_TERM, Optional.of(savedEntity.getId()),
                    Optional.of(savedEntity.getOntologyId()), Optional.empty());
                return Optional.of(new HttpCommandResponse(callback, true, termResponse, commandV2Entity.getId()));
            }):
    }

    @Transactional
    public Optional<HttpCommandResponse> updateTerm(long id, Long userId, UpdateTermRelation updateTerm,
                                                    LocalDateTime now) {
        return termRelationRepository.findById(id).filter(entity -> aclService.canWriteOntology(userId, entity.getOntologyId()))
            .map(termEntity -> {
                TermCommandPayload beforePayload = ontologyMapper.mapToTermCommandPayload(termEntity);
                updateTerm.name().ifPresent(termEntity::setName);
                updateTerm.color().ifPresent(termEntity::setColor);
                TermEntity savedEntity = termRelationRepository.save(termEntity);
                UpdateTermCommand updateCommand =
                    new UpdateTermCommand(id, beforePayload, ontologyMapper.mapToTermCommandPayload(savedEntity),
                        userId, termEntity.getOntologyId());

                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(updateCommand, now, now, userId));

                TermResponse termResponse = ontologyMapper.map(savedEntity);
                Callback callback = new Callback(Commands.UPDATE_TERM, Optional.of(savedEntity.getId()),
                    Optional.of(savedEntity.getOntologyId()), Optional.empty());
                return new HttpCommandResponse(callback, true, termResponse, commandV2Entity.getId());
            });
    }


    public Optional<HttpCommandResponse> undoDeleteTermRelation(UUID commandId,
                                                                DeleteTermRelationCommand deleteTermCommand,
                                                                Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return restoreTermRelation(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM_RELATION, now);
    }

    public Optional<HttpCommandResponse> redoDeleteTermRelation(UUID commandId,
                                                                DeleteTermRelationCommand deleteTermCommand,
                                                                Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return softDeleteTermRelation(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM_RELATION, now);
    }

    private Optional<HttpCommandResponse> restoreTermRelation(UUID commandId, Long termId, String command,
                                                              LocalDateTime now) {
        return termRelationRepository.findById(termId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    private Optional<HttpCommandResponse> softDeleteTermRelation(UUID commandId, Long termId, String command,
                                                                 LocalDateTime now) {
        return termRelationRepository.findById(termId).map(entity -> {
            entity.setDeleted(now);
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    private HttpCommandResponse saveAndBuildResponse(TermRelationEntity entity, String command, UUID commandId) {
        TermRelationEntity saved = termRelationRepository.save(entity);
        TermRelationResponse response = ontologyMapper.mapToTermRelationResponse(saved);
        Callback callback =
            new Callback(command, Optional.of(saved.getId()), Optional.of(saved.getOntologyId()), Optional.empty());
        return new HttpCommandResponse(callback, true, response, commandId);
    }

}
