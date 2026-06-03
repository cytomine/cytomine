package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.RelationRepository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

@Component
@RequiredArgsConstructor
public class TermRelationCommandService
    implements CRUDCommandService<CreateTermRelation, UpdateTermRelation, TermRelationCommandPayload> {
    private final TermRepository termRepository;
    private final TermRelationRepository termRelationRepository;
    private final RelationRepository relationRepository;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Override
    @Transactional
    public Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now) {
        return termRelationRepository.findById(id)
            .flatMap(entity -> termRepository.findById(entity.getTerm1Id()).map(term1 -> Pair.of(entity, term1)))
            .filter(pair -> aclService.canWriteOntology(userId, pair.getSecond().getOntologyId())).map(pair -> {
                TermRelationEntity termEntity = pair.getFirst();
                long ontologyId = pair.getSecond().getOntologyId();
                DeleteTermRelationCommand deleteCommand = new DeleteTermRelationCommand(id,
                    ontologyMapper.mapToTermRelationCommandPayload(termEntity, ontologyId), userId, ontologyId);
                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(deleteCommand, now, now, userId));
                termEntity.setDeleted(Timestamp.valueOf(now));
                return saveAndBuildResponse(termEntity, Commands.DELETE_TERM_RELATION, commandV2Entity.getId());
            });
    }

    @Override
    @Transactional
    public Optional<HttpCommandResponse> create(long userId, CreateTermRelation createTermRelation, LocalDateTime now) {
        return termRepository.findById(createTermRelation.term1Id())
            .filter(firstTerm -> aclService.canWriteOntology(userId, firstTerm.getOntologyId()))
            // check that the second term is in the same ontology
            .filter(firstTerm -> termRepository.findById(createTermRelation.term2Id())
                .map(secondTerm -> secondTerm.getOntologyId().equals(firstTerm.getOntologyId())).orElse(false))
            // Check that a relation between those terms don't exist before adding a new one.
            .filter(i -> termRelationRepository.findByTerm1IdAndTerm2Id(createTermRelation.term1Id(),
                createTermRelation.term2Id()).isEmpty()).map(firstTerm -> {
                long ontologyId = firstTerm.getOntologyId();
                long relationId = relationRepository.findParent().getId();
                TermRelationEntity termEntity =
                    ontologyMapper.mapToTermRelationEntity(createTermRelation, now, relationId);
                TermRelationEntity savedEntity = termRelationRepository.save(termEntity);
                TermRelationCommandPayload termCommandPayload =
                    ontologyMapper.mapToTermRelationCommandPayload(savedEntity, ontologyId);
                CreateTermRelationCommand insertTermCommand =
                    new CreateTermRelationCommand(termCommandPayload, userId, termCommandPayload.ontologyId());

                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(insertTermCommand, now, now, userId));

                TermRelationResponse termResponse = ontologyMapper.mapToTermRelationResponse(savedEntity);

                return new HttpCommandResponse(true, termResponse, commandV2Entity.getId(),
                    Commands.CREATE_TERM_RELATION);
            });
    }

    @Override
    @Transactional
    public Optional<HttpCommandResponse> update(long userId, long id, UpdateTermRelation updateTerm,
        LocalDateTime now) {
        return termRelationRepository.findById(id).flatMap(
                termRelationEntity -> termRepository.findById(termRelationEntity.getTerm1Id())
                    .map(termEntity -> Pair.of(termRelationEntity, termEntity)))
            .filter(pair -> aclService.canWriteOntology(userId, pair.getSecond().getOntologyId()))
            // check that the second term is in the same ontology
            .filter(pair -> termRepository.findById(pair.getFirst().getTerm2Id())
                .map(secondTerm -> secondTerm.getOntologyId().equals(pair.getSecond().getOntologyId())).orElse(false))
            .map(pair -> {
                TermRelationEntity termRelationEntity = pair.getFirst();
                TermEntity termEntity = pair.getSecond();
                long ontologyId = termEntity.getOntologyId();
                TermRelationCommandPayload beforePayload =
                    ontologyMapper.mapToTermRelationCommandPayload(termRelationEntity, ontologyId);
                updateTerm.term1Id().ifPresent(termRelationEntity::setTerm1Id);
                updateTerm.term2Id().ifPresent(termRelationEntity::setTerm2Id);
                TermRelationEntity savedEntity = termRelationRepository.save(termRelationEntity);
                UpdateTermRelationCommand updateCommand = new UpdateTermRelationCommand(id, beforePayload,
                    ontologyMapper.mapToTermRelationCommandPayload(savedEntity, ontologyId), userId, ontologyId);

                CommandV2Entity commandV2Entity =
                    commandV2Repository.save(commandMapper.map(updateCommand, now, now, userId));

                TermRelationResponse termResponse = ontologyMapper.mapToTermRelationResponse(savedEntity);
                return new HttpCommandResponse(true, termResponse, commandV2Entity.getId(),
                    Commands.UPDATE_TERM_RELATION);
            });
    }

    @Override
    public Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId,
        TermRelationCommandPayload payload, LocalDateTime now) {

        return termRelationRepository.findById(payload.id()).map(entity -> {
            entity.setTerm1Id(payload.term1Id());
            entity.setTerm2Id(payload.term2Id());
            return saveAndBuildResponse(entity, Commands.UPDATE_TERM_RELATION, commandId);
        });
    }

    @Override
    public Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, LocalDateTime now) {
        return termRelationRepository.findById(id).map(entity -> {
            entity.setDeleted(Timestamp.valueOf(now));
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    @Override
    public Optional<HttpCommandResponse> restore(UUID commandId, long id, String command, LocalDateTime now) {
        return termRelationRepository.findById(id).map(entity -> {
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

    private HttpCommandResponse saveAndBuildResponse(TermRelationEntity entity, String command, UUID commandId) {
        TermRelationEntity saved = termRelationRepository.save(entity);
        TermRelationResponse response = ontologyMapper.mapToTermRelationResponse(saved);
        return new HttpCommandResponse(true, response, commandId, command);
    }

}
