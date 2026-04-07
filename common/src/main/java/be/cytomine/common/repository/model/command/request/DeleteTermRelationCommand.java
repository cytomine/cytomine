package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

import static java.lang.String.format;

public record DeleteTermRelationCommand(Long id, TermRelationCommandPayload before, long userId, long ontologyId)
    implements DeleteCommandRequest<UpdateCommandPayload<TermRelationCommandPayload>> {
    @Override
    public UpdateCommandPayload<TermRelationCommandPayload> data() {
        return new UpdateCommandPayload<>(Optional.of(before), Optional.empty());
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM_RELATION;
    }

    @Override
    public String getActionMessage() {
        return format("Term relation %s deleted in ontology %s", before.id(), ontologyId);
    }
}
