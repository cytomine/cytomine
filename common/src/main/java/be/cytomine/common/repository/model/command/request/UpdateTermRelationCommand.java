package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;

import static java.lang.String.format;

public record UpdateTermRelationCommand(Long id, TermRelationCommandPayload before, TermRelationCommandPayload after,
                                        long userId, Long ontologyId)
    implements UpdateCommandRequest<TermRelationCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s => %s),color (%s => %s) updated in ontology %s", before.id(), before.name(),
            after.name(), ontologyId);
    }
}
