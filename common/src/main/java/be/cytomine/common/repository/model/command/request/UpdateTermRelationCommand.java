package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;

import static java.lang.String.format;

public record UpdateTermRelationCommand(Long id, TermRelationCommandPayload before, TermRelationCommandPayload after,
                                        long userId, Long ontologyId)
    implements UpdateCommandRequest<TermRelationCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM_RELATION;
    }

    @Override
    public String getActionMessage() {
        return format("Term relation %s (term1: %s => %s, term2: %s => %s) updated in ontology %s",
            before.id(), before.term1Id(), after.term1Id(), before.term2Id(), after.term2Id(), ontologyId);
    }
}
