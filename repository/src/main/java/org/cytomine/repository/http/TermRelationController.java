package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.TermRelationMapper;
import org.cytomine.repository.persistence.RelationRepository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.TermRelationCommandService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

import static be.cytomine.common.repository.http.TermRelationHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TermRelationController implements TermRelationHttpContract {
    private final TermRelationMapper termRelationMapper;
    private final TermRelationRepository termRelationRepository;
    private final org.cytomine.repository.persistence.TermRepository termRepository;
    private final RelationRepository relationRepository;
    private final TermRelationCommandService termRelationCommandService;
    private final ACLService aclService;
    private final CurrentUserService currentUserService;

    @Override
    public List<TermRelationResponse> findAllByOntologyId(long ontologyId) {
        if (!aclService.canReadOntology(currentUserService.getCurrentUserId(), ontologyId)) {
            return List.of();
        }
        return termRelationRepository.findAllByOntologyId(ontologyId)
            .stream()
            .map(termRelationMapper::mapToTermRelationResponse)
            .toList();
    }

    @Override
    public Set<Long> findAllIdsByOntologyId(long ontologyId) {
        if (!aclService.canReadOntology(currentUserService.getCurrentUserId(), ontologyId)) {
            return Set.of();
        }
        return termRelationRepository.findAllByOntologyId(ontologyId)
            .stream()
            .map(TermRelationEntity::getId)
            .collect(toSet());
    }

    @Override
    public Optional<TermRelationResponse> findTermRelationByID(long id) {
        return termRelationRepository.findByIdAndDeletedNull(id)
            .flatMap(termEntity -> termRepository.findByIdAndDeletedNull(termEntity.getTerm1Id())
                .filter(term1 -> aclService.canReadOntology(currentUserService.getCurrentUserId(),
                    term1.getOntologyId()))
                .map(term1 -> termRelationMapper.mapToTermRelationResponse(termEntity)));
    }

    @Override
    public Set<Long> findTermRelationsIdsByTermId(long termId) {
        return termRelationRepository.findAllByTerm1IdOrTerm2Id(termId, termId)
            .stream()
            .map(TermRelationEntity::getId)
            .collect(toSet());
    }

    @Override
    public Optional<HttpCommandResponse> create(CreateTermRelation createTermRelation) {
        return termRelationCommandService.create(currentUserService.getCurrentUserId(), createTermRelation,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, UpdateTermRelation updateTermRelation) {
        return termRelationCommandService.update(currentUserService.getCurrentUserId(),
            id,
            updateTermRelation,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id) {
        return termRelationCommandService.delete(currentUserService.getCurrentUserId(), id,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Set<HttpCommandResponse> deleteAll(Set<Long> ids) {
        return ids.stream()
            .map(id -> termRelationCommandService.delete(currentUserService.getCurrentUserId(), id,
                LocalDateTime.now().truncatedTo(MICROS)))
            .flatMap(Optional::stream)
            .collect(toSet());
    }

    @Override
    public Optional<HttpCommandResponse> deleteByTerms(@PathVariable long idTerm1,
        @PathVariable long idTerm2) {
        long parentRelationId = relationRepository.findParent().getId();
        return termRelationRepository.findByRelationIdAndTerm1IdAndTerm2Id(parentRelationId, idTerm1, idTerm2)
            .flatMap(entity -> termRelationCommandService.delete(currentUserService.getCurrentUserId(),
                entity.getId(),
                LocalDateTime.now().truncatedTo(MICROS)));
    }
}
