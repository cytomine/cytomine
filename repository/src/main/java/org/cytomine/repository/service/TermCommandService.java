package org.cytomine.repository.service;

import java.util.Optional;

import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.delete.DeleteTermCommand;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandService commandService;

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

    public Optional<HttpCommandResponse<TermResponse>> createTerm(CreateTerm createTerm) {
        return null;
    }

    public Optional<HttpCommandResponse<TermResponse>> updateTerm(long id, UpdateTerm updateTerm) {
        termRepository.findById(id).map(entity -> {
            updateTerm.name().ifPresent(entity::setName);
            updateTerm.color().ifPresent(entity::setColor);
            return ontologyMapper.map(termRepository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Term not found: " + id));
        return null;
    }

}
