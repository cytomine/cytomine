package org.cytomine.repository.service;

import java.util.Optional;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.TermCommandPayload;
import be.cytomine.common.repository.model.command.delete.DeleteTermCommand;
import be.cytomine.common.repository.model.command.insert.InsertTermCommand;
import be.cytomine.common.repository.model.command.update.UpdateTermCommand;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandService commandService;

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id, Long userId) {
        return termRepository.findById(id).map(termEntity -> {
            DeleteTermCommand deleteCommand =
                new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity),
                    userId, null);
            CommandEntity commandEntity = commandService.delete(deleteCommand);
            TermResponse termResponse = ontologyMapper.map(termEntity);
            termRepository.deleteById(id);
            return new HttpCommandResponse<>(null, null, true, termResponse, commandEntity.getId());
        });
    }

    public Optional<HttpCommandResponse<TermResponse>> createTerm(Long userId,
                                                                  CreateTerm createTerm) {
        TermEntity termEntity = ontologyMapper.map(createTerm);
        TermCommandPayload termCommandPayload = ontologyMapper.mapToTermCommandPayload(termEntity);
        InsertTermCommand insertTermCommand =
            new InsertTermCommand(termCommandPayload, userId, null);
        CommandEntity commandEntity = commandService.insert(insertTermCommand);
        TermResponse termResponse = ontologyMapper.map(termRepository.save(termEntity));
        return Optional.of(new HttpCommandResponse<>(null, null, true, termResponse,
            commandEntity.getId()));

    }

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> updateTerm(long id, Long userId,
                                                                  UpdateTerm updateTerm) {
        return termRepository.findById(id).map(termEntity -> {
            updateTerm.name().ifPresent(termEntity::setName);
            updateTerm.color().ifPresent(termEntity::setColor);

            UpdateTermCommand updateCommand =
                new UpdateTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity),
                    userId, null);
            CommandEntity commandEntity = commandService.update(updateCommand);
            TermResponse termResponse = ontologyMapper.map(termRepository.save(termEntity));
            return new HttpCommandResponse<>(null, null, true, termResponse, commandEntity.getId());
        });

    }

}
