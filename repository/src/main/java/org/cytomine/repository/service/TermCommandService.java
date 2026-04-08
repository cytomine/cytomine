package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> deleteTerm(Long id, Long userId, LocalDateTime now) {
        return termRepository.findById(id)
            .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId())).map(termEntity -> {
                DeleteTermCommand deleteCommand =
                    new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity), userId,
                        termEntity.getOntologyId());
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termEntity.setDeleted(now);
                return saveAndBuildResponse(termEntity, Commands.DELETE_TERM, commandV2Entity.getId());
            });
    }

    public Optional<HttpCommandResponse> createTerm(Long userId, CreateTerm createTerm, LocalDateTime now) {
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
        return Optional.of(new HttpCommandResponse(true, termResponse, commandV2Entity.getId()));
    }

    @Transactional
    public Optional<HttpCommandResponse> updateTerm(long id, Long userId, UpdateTerm updateTerm, LocalDateTime now) {
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

                return new HttpCommandResponse(true, termResponse, commandV2Entity.getId());
            });
    }

    public Optional<HttpCommandResponse> undoDeleteTerm(UUID commandId, DeleteTermCommand deleteTermCommand,
                                                        Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return restoreTerm(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM, now);
    }

    public Optional<HttpCommandResponse> redoCreateTerm(UUID commandId, CreateTermCommand createTermCommand,
                                                        Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, createTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return restoreTerm(commandId, createTermCommand.after().id(), Commands.CREATE_TERM, now);
    }

    public Optional<HttpCommandResponse> undoCreateTerm(UUID commandId, CreateTermCommand createTermCommand,
                                                        Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, createTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return softDeleteTerm(commandId, createTermCommand.after().id(), Commands.UPDATE_TERM, now);
    }

    public Optional<HttpCommandResponse> redoDeleteTerm(UUID commandId, DeleteTermCommand deleteTermCommand,
                                                        Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return softDeleteTerm(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM, now);
    }

    public Optional<HttpCommandResponse> redoUpdateTerm(UUID commandId, UpdateTermCommand updateTermCommand,
                                                        Long userId, LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, updateTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return termRepository.findById(updateTermCommand.after().id()).map(entity -> {
            entity.setColor(updateTermCommand.after().color());
            entity.setName(updateTermCommand.after().name());
            entity.setUpdated(now);
            return saveAndBuildResponse(entity, Commands.UPDATE_TERM, commandId);
        });
    }

    public Optional<HttpCommandResponse> undoUpdateTerm(UUID commandId, UpdateTermCommand updateTermCommand,
                                                        Long userId) {
        if (!aclService.canWriteOntology(userId, updateTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return termRepository.findById(updateTermCommand.after().id()).map(entity -> {
            entity.setColor(updateTermCommand.before().color());
            entity.setName(updateTermCommand.before().name());
            return saveAndBuildResponse(entity, Commands.CREATE_TERM, commandId);
        });
    }

    private Optional<HttpCommandResponse> restoreTerm(UUID commandId, Long termId, String command, LocalDateTime now) {
        return termRepository.findById(termId).map(entity -> {
            entity.setDeleted(null);
            entity.setUpdated(now);
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    private Optional<HttpCommandResponse> softDeleteTerm(UUID commandId, Long termId, String command,
                                                         LocalDateTime now) {
        return termRepository.findById(termId).map(entity -> {
            entity.setDeleted(now);
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    private HttpCommandResponse saveAndBuildResponse(TermEntity entity, String command, UUID commandId) {
        TermEntity saved = termRepository.save(entity);
        TermResponse response = ontologyMapper.map(saved);
        return new HttpCommandResponse(true, response, commandId);
    }
}
