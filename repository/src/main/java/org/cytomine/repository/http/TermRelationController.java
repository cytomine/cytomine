package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TermRelationCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
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
    private final TermRelationCommandService termRelationCommandService;
    private final ACLService aclService;

    @Override
    @GetMapping("/{id}")
    public Optional<TermRelationResponse> findTermByID(long id, long userId) {
        return termRelationRepository.findById(id)
            .filter(termEntity -> aclService.canReadOntology(userId, termEntity.getOntologyId()))
            .map(ontologyMapper::mapToTermRelationResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateTermRelation createTermRelation) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateTermRelation updateTermRelation) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return termRelationCommandService.deleteTermRelation(id, userId, LocalDateTime.now());
    }
}
