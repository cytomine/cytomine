package be.cytomine.common.repository.model.command.delete;

import java.util.Optional;

import be.cytomine.common.repository.model.command.TermCommandPayload;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload data,
    Long userId,
    Long internalOntologyId
) implements DeleteCommandRequest<TermCommandPayload> {

    public static final String SERVICE_NAME = "TermService";

    @Override
    public Optional<Long> ontologyId() {
        return Optional.of(internalOntologyId);
    }

    @Override
    public Optional<Long> projectId() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> storageId() {
        return Optional.empty();
    }

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
