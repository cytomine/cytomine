package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;

import static java.lang.String.format;

public record CreateTermRelationCommand(TermRelationCommandPayload after, long userId, long ontologyId)
    implements CreateCommandRequest<TermRelationCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM_RELATION;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", after.id(), after.name(), ontologyId);
    }
}
