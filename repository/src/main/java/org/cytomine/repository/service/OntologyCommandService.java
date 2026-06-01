package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.OntologyRepository;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@RequiredArgsConstructor
@Component
public class OntologyCommandService
    implements CRUDCommandService<CreateOntology, UpdateOntology, OntologyCommandPayload> {
    private final OntologyRepository ontologyRepository;
    private final ACLService aclService;

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateOntology createPayload, LocalDateTime now) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> update(long userId, long id, UpdateOntology updatePayload, LocalDateTime now) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId,
        OntologyCommandPayload payload, LocalDateTime now) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, LocalDateTime now) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> restore(UUID commandId, long id, String command, LocalDateTime now) {
        return Optional.empty();
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
