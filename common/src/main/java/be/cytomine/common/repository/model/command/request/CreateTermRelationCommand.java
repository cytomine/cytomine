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
        return format("Term relation %s (term1: %s, term2: %s) added in ontology %s",
            after.id(), after.term1Id(), after.term2Id(), ontologyId);
    }
}
