package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.OntologyRepository;
import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@RequiredArgsConstructor
@Component
@Getter
public class OntologyCommandService
    implements
    CRUDCommandService<CreateOntology, UpdateOntology, OntologyCommandPayload, OntologyEntity, OntologyResponse> {
    private final OntologyRepository ontologyRepository;
    private final ACLService aclService;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;

    @Override
    public OntologyEntity save(OntologyEntity entity) {
        return null;
    }

    @Override
    public OntologyCommandPayload map(OntologyEntity entity) {
        return null;
    }

    @Override
    public OntologyResponse mapToResponse(OntologyEntity entity) {
        return null;
    }

    @Override
    public Optional<OntologyEntity> get(long id) {
        return Optional.empty();
    }

    @Override
    public OntologyEntity update(OntologyEntity entity, UpdateOntology updatePayload, Timestamp now) {
        return ontologyMapper.update(entity, updatePayload.name().orElse(entity.getName()), now);
    }

    @Override
    public boolean canWrite(long userId, long id) {
        return aclService.canWriteOntology(userId, id);
    }

    @Override
    public boolean canDelete(long userId, long id) {
        return aclService.canDeleteOntology(userId, id);
    }
}
