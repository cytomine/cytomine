package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Date;
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

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.Callback;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.CreateTermCommand;
import be.cytomine.common.repository.model.command.DeleteTermCommand;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.UpdateTermCommand;
import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id, Long userId, LocalDateTime now) {
        return termRepository.findById(id)
                   .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId())).map(termEntity -> {
                DeleteTermCommand deleteCommand =
                    new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity), userId,
                        termEntity.getOntologyId());
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termEntity.setDeleted(now);

                Callback callback = new Callback(Commands.DELETE_TERM, Optional.of(termEntity.getId()),
                    Optional.of(termEntity.getOntologyId()), Optional.empty());
                TermEntity deleted = termRepository.save(termEntity);
                TermResponse termResponse = ontologyMapper.map(deleted);
                return new HttpCommandResponse<>(callback, true, termResponse, commandV2Entity.getId());
            });
    }

    public Optional<HttpCommandResponse<TermResponse>> createTerm(Long userId, CreateTerm createTerm,
                                                                  LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, createTerm.ontology())) {
            return Optional.empty();
        }

        TermEntity termEntity = ontologyMapper.map(createTerm, new Date());

        TermEntity savedEntity = termRepository.save(termEntity);
        TermCommandPayload termCommandPayload = ontologyMapper.mapToTermCommandPayload(savedEntity);
        CreateTermCommand insertTermCommand =
            new CreateTermCommand(termCommandPayload, userId, termCommandPayload.ontology());

        CommandV2Entity commandV2Entity =
            commandV2Repository.save(commandMapper.map(insertTermCommand, now, now, userId));

        TermResponse termResponse = ontologyMapper.map(savedEntity);
        Callback callback = new Callback(Commands.CREATE_TERM, Optional.of(savedEntity.getId()),
            Optional.of(savedEntity.getOntologyId()), Optional.empty());
        return Optional.of(new HttpCommandResponse<>(callback, true, termResponse, commandV2Entity.getId()));
    }

    public Optional<HttpCommandResponse<TermResponse>> undoDeleteTerm(UUID commandId,
                                                                      DeleteTermCommand deleteTermCommand, Long userId,
                                                                      LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return termRepository.findById(deleteTermCommand.before().id()).map(termEntity -> {
            termEntity.setDeleted(null);
            termEntity.setUpdated(now);
            TermEntity savedEntity = termRepository.save(termEntity);
            TermResponse termResponse = ontologyMapper.map(savedEntity);
            Callback callback = new Callback(Commands.DELETE_TERM, Optional.of(savedEntity.getId()),
                Optional.of(savedEntity.getOntologyId()), Optional.empty());
            return new HttpCommandResponse<>(callback, true, termResponse, commandId);
        });


    }

    public Optional<HttpCommandResponse<TermResponse>> undoCreateTerm(UUID commandId,
                                                                      CreateTermCommand createTermCommand, Long userId,
                                                                      LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, createTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return termRepository.findById(createTermCommand.after().id()).map(termEntity -> {
            termEntity.setDeleted(now);
            TermEntity savedEntity = termRepository.save(termEntity);
            TermResponse termResponse = ontologyMapper.map(savedEntity);
            Callback callback = new Callback(Commands.CREATE_TERM, Optional.of(savedEntity.getId()),
                Optional.of(savedEntity.getOntologyId()), Optional.empty());
            return new HttpCommandResponse<>(callback, true, termResponse, commandId);
        });


    }

    public Optional<HttpCommandResponse<TermResponse>> undoUpdateTerm(UUID commandId,
                                                                      UpdateTermCommand updateTermCommand, Long userId,
                                                                      LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, updateTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return termRepository.findById(updateTermCommand.after().id()).map(termEntity -> {
            termEntity.setColor(updateTermCommand.before().color());
            termEntity.setName(updateTermCommand.before().name());
            TermEntity savedEntity = termRepository.save(termEntity);
            TermResponse termResponse = ontologyMapper.map(savedEntity);
            Callback callback = new Callback(Commands.CREATE_TERM, Optional.of(savedEntity.getId()),
                Optional.of(savedEntity.getOntologyId()), Optional.empty());
            return new HttpCommandResponse<>(callback, true, termResponse, commandId);
        });


    }

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> updateTerm(long id, Long userId, UpdateTerm updateTerm,
                                                                  LocalDateTime now) {
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
                       Callback callback = new Callback(Commands.UPDATE_TERM, Optional.of(savedEntity.getId()),
                           Optional.of(savedEntity.getOntologyId()), Optional.empty());
                       return new HttpCommandResponse<>(callback, true, termResponse, commandV2Entity.getId());
                   });
    }

}
