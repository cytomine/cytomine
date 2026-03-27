package org.cytomine.repository.service;

import java.util.Date;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.Callback;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.TermCommandPayload;
import be.cytomine.common.repository.model.command.delete.DeleteTermCommand;
import be.cytomine.common.repository.model.command.insert.InsertTermCommand;
import be.cytomine.common.repository.model.command.update.UpdateTermCommand;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandService commandService;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id, Long userId) {
        return termRepository.findById(id)
                   .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId()))
                   .map(termEntity -> {
                       DeleteTermCommand deleteCommand =
                           new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity),
                               userId, null);
                       CommandEntity commandEntity = commandService.delete(deleteCommand);
                       TermResponse termResponse = ontologyMapper.map(termEntity);
                       Callback callback = new Callback("be.cytomine.DeleteTermCommand",
                           Optional.of(termEntity.getId()), Optional.of(termEntity.getOntologyId()), Optional.empty());
                       termRepository.deleteById(id);
                       return new HttpCommandResponse<>(callback, true, termResponse, commandEntity.getId());
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
            new InsertTermCommand(termCommandPayload, userId, null);
        CommandEntity commandEntity = commandService.insert(insertTermCommand);
        TermEntity savedEntity = termRepository.save(termEntity);
        TermResponse termResponse = ontologyMapper.map(savedEntity);
        Callback callback = new Callback("be.cytomine.AddTermCommand",
            Optional.of(savedEntity.getId()), Optional.of(savedEntity.getOntologyId()), Optional.empty());
        return Optional.of(new HttpCommandResponse<>(callback, true, termResponse,
            commandEntity.getId()));
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
                       CommandEntity commandEntity = commandService.update(updateCommand);
                       TermEntity savedEntity = termRepository.save(termEntity);
                       TermResponse termResponse = ontologyMapper.map(savedEntity);
                       Callback callback = new Callback("be.cytomine.EditTermCommand",
                           Optional.of(savedEntity.getId()), Optional.of(savedEntity.getOntologyId()),
                           Optional.empty());
                       return new HttpCommandResponse<>(callback, true, termResponse, commandEntity.getId());
                   });
    }

}
