package be.cytomine.common.repository.model.command;

import static java.lang.String.format;

public record InsertTermCommand(TermCommandPayload data, long userId, long ontologyId)
    implements InsertCommandRequest<TermCommandPayload> {


    @Override

    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", data.id(), data.name(), ontologyId);
    }
}
