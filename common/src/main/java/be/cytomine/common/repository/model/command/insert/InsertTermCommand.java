package be.cytomine.common.repository.model.command.insert;

import java.util.Optional;

import be.cytomine.common.repository.model.command.TermCommandPayload;

import static java.lang.String.format;

public record InsertTermCommand(TermCommandPayload data, Long userId, Long ontologyId)
    implements InsertCommandRequest<TermCommandPayload> {


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
        return format("Term %s (%s) added in ontology %s", data.id(), data.name(), ontologyId);
    }
}
