package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.term.UpdateTermCommandPayload;

import static java.lang.String.format;

public record CreateTermCommand(TermCommandPayload after, long userId, long ontologyId)
    implements CreateCommandRequest<UpdateTermCommandPayload> {

    @Override
    public UpdateTermCommandPayload data() {
        return new UpdateTermCommandPayload(Optional.empty(), Optional.of(after));
    }

    @Override

    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", after.id(), after.name(), ontologyId);
    }
}
