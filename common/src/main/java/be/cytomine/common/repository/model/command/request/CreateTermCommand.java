package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record CreateTermCommand(TermCommandPayload after, long userId, long ontologyId)
    implements CreateCommandRequest<TermCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", after.id(), after.name(), ontologyId);
    }
}
