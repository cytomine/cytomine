package org.cytomine.repository.service;

import java.util.Optional;

import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.delete.DeleteTermCommand;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class TermCommandService {

    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandService commandService;
    private final ObjectMapper objectMapper;

    public Optional<HttpCommandResponse<TermResponse>> deleteTerm(Long id, Long userId) {
        return termRepository.findById(id).map(termEntity -> {

            DeleteTermCommand deleteCommand =
                new DeleteTermCommand(id, ontologyMapper.mapToTermCommandPayload(termEntity), userId, null);
            CommandEntity commandEntity = commandService.delete(deleteCommand);
            termRepository.delete(termEntity);
            TermResponse termResponse = ontologyMapper.map(termEntity);
            return new HttpCommandResponse<>(null, null, true, termResponse, commandEntity.getId());
        });
    }

    private String serializeToJson(TermEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize entity to JSON", e);
        }
    }
}
