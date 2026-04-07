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
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Callback;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;

@Component
@AllArgsConstructor
public class TermRelationCommandService {
    private final TermRelationRepository termRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Transactional
    public Optional<HttpCommandResponse> deleteTerm(Long id, Long userId, LocalDateTime now) {
        return termRepository.findById(id)
            .filter(entity -> aclService.canDeleteOntology(userId, entity.getOntologyId()))
            .map(termEntity -> {
                DeleteTermRelationCommand deleteCommand =
                    new DeleteTermRelationCommand(id, ontologyMapper.mapToTermRelationCommandPayload(termEntity), userId,
                        termEntity.getOntologyId());
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termEntity.setDeleted(now);
                return saveAndBuildResponse(termEntity, Commands.DELETE_TERM_RELATION, commandV2Entity.getId());
            });
    }

    public Optional<HttpCommandResponse> undoDeleteTerm(UUID commandId,
                                                                      DeleteTermRelationCommand deleteTermCommand, Long userId,
                                                                      LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return restoreTerm(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM_RELATION, now);
    }

    public Optional<HttpCommandResponse> redoDeleteTerm(UUID commandId,
                                                                      DeleteTermRelationCommand deleteTermCommand, Long userId,
                                                                      LocalDateTime now) {
        if (!aclService.canWriteOntology(userId, deleteTermCommand.ontologyId())) {
            return Optional.empty();
        }
        return softDeleteTerm(commandId, deleteTermCommand.before().id(), Commands.DELETE_TERM_RELATION, now);
    }

    private Optional<HttpCommandResponse> restoreTerm(UUID commandId, Long termId, String command,
                                                                    LocalDateTime now) {
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

    private HttpCommandResponse saveAndBuildResponse(TermRelationEntity entity, String command, UUID commandId) {
        TermRelationEntity saved = termRepository.save(entity);
        TermRelationResponse response = ontologyMapper.mapToTermRelationResponse(saved);
        Callback callback =
            new Callback(command, Optional.of(saved.getId()), Optional.of(saved.getOntologyId()), Optional.empty());
        return new HttpCommandResponse(callback, true, response, commandId);
    }

}
