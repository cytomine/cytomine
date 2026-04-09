package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record DeleteTermCommand(Long id, TermCommandPayload before, long userId, long ontologyId)
    implements DeleteCommandRequest<TermCommandPayload> {

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", before.id(), before.name(), ontologyId);
    }
}
