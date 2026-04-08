package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.term.UpdateTermCommandPayload;

import static java.lang.String.format;

public record UpdateTermCommand(Long id, TermCommandPayload before, TermCommandPayload after, long userId,
                                Long ontologyId)
    implements UpdateCommandRequest<UpdateTermCommandPayload> {

    @Override
    public UpdateTermCommandPayload data() {
        return new UpdateTermCommandPayload(Optional.of(before), Optional.of(after));
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s => %s),color (%s => %s) updated in ontology %s", before.id(), before.name(),
            after.name(), before.color(), after.color(), ontologyId);
    }
}
