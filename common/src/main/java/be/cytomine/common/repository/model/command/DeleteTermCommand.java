package be.cytomine.common.repository.model.command;

import java.util.Optional;

import static java.lang.String.format;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload data,
    Long userId,
    Long ontologyId
) implements DeleteCommandRequest<TermCommandPayload> {

    @Override
    public Optional<Long> getProjectId() {
        return Optional.empty();
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", data.id(), data.name(), ontologyId);
    }
}
