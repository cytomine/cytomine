package be.cytomine.common.repository.model.command;

import java.util.Optional;

import static java.lang.String.format;

public record UpdateTermCommand(Long id, TermCommandPayload data, Long userId, Long ontologyId)
    implements UpdateCommandRequest<TermCommandPayload> {

    @Override
    public Optional<Long> projectId() {
        return Optional.empty();
    }

    @Override
    public DataType dataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", data.id(), data.name(), ontologyId);
    }
}
