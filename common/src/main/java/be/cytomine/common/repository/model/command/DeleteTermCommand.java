package be.cytomine.common.repository.model.command;

import static java.lang.String.format;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload data,
    long userId,
    long ontologyId
) implements DeleteCommandRequest<TermCommandPayload> {


    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", data.id(), data.name(), ontologyId);
    }
}
