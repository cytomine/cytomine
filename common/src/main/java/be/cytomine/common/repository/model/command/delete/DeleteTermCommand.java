package be.cytomine.common.repository.model.command.delete;

import java.util.Optional;

import be.cytomine.common.repository.model.command.TermCommandPayload;

import static java.lang.String.format;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload data,
    Long userId,
    Long ontologyId
) implements DeleteCommandRequest<TermCommandPayload> {

    @Override
    public Optional<Long> projectId() {
        return Optional.empty();
    }

    @Override
    public String serviceName() {
        return "TermService";
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", data.id(), data.name(), ontologyId);
    }
}
