package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.RelationRepository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TermRelationCommandService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

import static be.cytomine.common.repository.http.TermRelationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class TermRelationController implements TermRelationHttpContract {
    private final OntologyMapper ontologyMapper;
    private final TermRelationRepository termRelationRepository;
    private final org.cytomine.repository.persistence.TermRepository termRepository;
    private final RelationRepository relationRepository;
    private final TermRelationCommandService termRelationCommandService;
    private final ACLService aclService;

    @Override
    @GetMapping("/ontology/{ontologyId}")
    public List<TermRelationResponse> findAllByOntologyId(long ontologyId, long userId) {
        if (!aclService.canReadOntology(userId, ontologyId)) {
            return List.of();
        }
        return termRelationRepository.findAllByOntologyId(ontologyId).stream()
            .map(e -> ontologyMapper.mapToTermRelationResponse(e, ontologyId))
            .toList();
    }

    @Override
    @GetMapping("/{id}")
    public Optional<TermRelationResponse> findTermRelationByID(long id, long userId) {
        return termRelationRepository.findById(id)
            .flatMap(termEntity -> termRepository.findById(termEntity.getTerm1Id())
                .filter(term1 -> aclService.canReadOntology(userId, term1.getOntologyId()))
                .map(term1 -> ontologyMapper.mapToTermRelationResponse(termEntity, term1.getOntologyId())));
    }

    @Override
    public Set<TermRelationResponse> findTermRelationsByTermID(long termId, long userId) {
        return Set.of();
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateTermRelation createTermRelation) {
        return termRelationCommandService.createTermRelation(userId, createTermRelation, LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateTermRelation updateTermRelation) {
        return termRelationCommandService.updateTerm(id, userId, updateTermRelation, LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return termRelationCommandService.deleteTermRelation(id, userId, LocalDateTime.now());
    }

    @Override
    @DeleteMapping("/term1/{idTerm1}/term2/{idTerm2}")
    public Optional<HttpCommandResponse> deleteByTerms(@PathVariable long idTerm1, @PathVariable long idTerm2,
                                                       @RequestParam long userId) {
        long parentRelationId = relationRepository.findParent().getId();
        return termRelationRepository.findByRelationIdAndTerm1IdAndTerm2Id(parentRelationId, idTerm1, idTerm2)
            .flatMap(
                entity -> termRelationCommandService.deleteTermRelation(entity.getId(), userId, LocalDateTime.now()));
    }
}
