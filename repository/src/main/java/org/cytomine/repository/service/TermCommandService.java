package org.cytomine.repository.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

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
import be.cytomine.common.repository.model.command.DeleteTermCommand;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.InsertTermCommand;
import be.cytomine.common.repository.model.command.TermCommandPayload;
import be.cytomine.common.repository.model.command.UpdateTermCommand;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id, Long userId) {
        return termRepository.findById(id)
                   .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId()))
                   .map(termEntity -> {
                       DeleteTermCommand deleteCommand =
                           new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity),
                               userId, termEntity.getOntologyId());
                       ZonedDateTime now = ZonedDateTime.now();
                       CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(deleteCommand,
                           now, now, userId));
                       TermResponse termResponse = ontologyMapper.map(termEntity);
                       Callback callback = new Callback(Commands.DELETE_TERM,
                           Optional.of(termEntity.getId()), Optional.of(termEntity.getOntologyId()), Optional.empty());
                       termRepository.deleteById(id);
                       return new HttpCommandResponse<>(callback, true, termResponse, commandV2Entity.getId());
                   });
    }

    public Optional<HttpCommandResponse<TermResponse>> createTerm(Long userId,
                                                                  CreateTerm createTerm) {
        if (!aclService.canWriteOntology(userId, createTerm.ontology())) {
            return Optional.empty();
        }

        TermEntity termEntity = ontologyMapper.map(createTerm, new Date());
        TermCommandPayload termCommandPayload = ontologyMapper.mapToTermCommandPayload(termEntity);
        InsertTermCommand insertTermCommand =
            new InsertTermCommand(termCommandPayload, userId, termCommandPayload.ontology());
        ZonedDateTime now = ZonedDateTime.now();
        CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(insertTermCommand,
            now, now, userId));
        TermEntity savedEntity = termRepository.save(termEntity);
        TermResponse termResponse = ontologyMapper.map(savedEntity);
        Callback callback = new Callback(Commands.INSERT_TERM,
            Optional.of(savedEntity.getId()), Optional.of(savedEntity.getOntologyId()), Optional.empty());
        return Optional.of(new HttpCommandResponse<>(callback, true, termResponse,
            commandV2Entity.getId()));
    }

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> updateTerm(long id, Long userId,
                                                                  UpdateTerm updateTerm) {
        return termRepository.findById(id)
                   .filter(entity -> aclService.canWriteOntology(userId, entity.getOntologyId()))
                   .map(termEntity -> {
                       updateTerm.name().ifPresent(termEntity::setName);
                       updateTerm.color().ifPresent(termEntity::setColor);

                       UpdateTermCommand updateCommand =
                           new UpdateTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity),
                               userId, null);
                       ZonedDateTime now = ZonedDateTime.now();
                       CommandV2Entity commandV2Entity = commandV2Repository.save(commandMapper.map(updateCommand,
                           now, now, userId));
                       TermEntity savedEntity = termRepository.save(termEntity);
                       TermResponse termResponse = ontologyMapper.map(savedEntity);
                       Callback callback = new Callback(Commands.UPDATE_TERM,
                           Optional.of(savedEntity.getId()), Optional.of(savedEntity.getOntologyId()),
                           Optional.empty());
                       return new HttpCommandResponse<>(callback, true, termResponse, commandV2Entity.getId());
                   });
    }

}
