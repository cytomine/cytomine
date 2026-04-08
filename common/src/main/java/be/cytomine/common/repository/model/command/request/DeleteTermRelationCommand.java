package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;

import static java.lang.String.format;

public record DeleteTermRelationCommand(Long id, TermRelationCommandPayload before, long userId, long ontologyId)
    implements DeleteCommandRequest<TermRelationCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM_RELATION;
    }

    @Override
    public String getActionMessage() {
        return format("Term relation %s (term1: %s, term2: %s) deleted in ontology %s",
            before.id(), before.term1Id(), before.term2Id(), ontologyId);
    }
}
