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
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateOntologyCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@RequiredArgsConstructor
@Component
@Getter
public class OntologyCommandService implements
    CRUDCommandService<CreateOntology, UpdateOntology, OntologyCommandPayload, OntologyEntity, OntologyResponse> {
    private final OntologyRepository ontologyRepository;
    private final ACLService aclService;
    private final OntologyMapper ontologyMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;

    @Override
    public OntologyEntity save(OntologyEntity entity) {
        return ontologyRepository.save(entity);
    }

    @Override
    public UpdateCommandRequest<OntologyCommandPayload> mapUpdateCommand(long userId, OntologyCommandPayload before,
        OntologyCommandPayload after) {
        return new UpdateOntologyCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<OntologyCommandPayload> mapCreateCommand(long userId, OntologyCommandPayload after) {
        return new CreateOntologyCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<OntologyCommandPayload> mapDeleteCommand(long userId, OntologyCommandPayload before) {
        return new DeleteOntologyCommand(before, userId);
    }

    @Override
    public Optional<OntologyEntity> get(long id) {
        return ontologyRepository.findById(id);
    }

    @Override
    public OntologyEntity updateEntityWithEntity(OntologyEntity entity, UpdateOntology updatePayload, Timestamp now) {
        return ontologyMapper.update(entity, updatePayload.name()
            .orElse(entity.getName()), now);
    }

    @Override
    public OntologyEntity updateEntityWithPayload(OntologyEntity entity, OntologyCommandPayload payload,
        Timestamp now) {
        return ontologyMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public OntologyResponse mapToResponse(OntologyEntity entity) {
        return ontologyMapper.mapToOntologyResponse(entity);
    }

    @Override
    public OntologyEntity mapCreateToEntity(CreateOntology createPayload, long userId, Timestamp creationDate) {
        return ontologyMapper.mapToOntologyEntity(createPayload, userId, creationDate);
    }

    @Override
    public OntologyCommandPayload map(OntologyEntity entity) {
        return ontologyMapper.mapToOntologyCommandPayload(entity);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.canWriteOntology(userId, id);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.canDeleteOntology(userId, id);
    }
    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.canWriteOntology(userId, id);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.canDeleteOntology(userId, id);
    }
}
