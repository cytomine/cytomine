package be.cytomine.common.repository.model.command;

import java.util.Optional;

import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.term.UpdateTermCommandPayload;

import static java.lang.String.format;

public record DeleteTermCommand(
    Long id,
    TermCommandPayload before,
    long userId,
    long ontologyId
) implements DeleteCommandRequest<UpdateTermCommandPayload> {


    @Override
    public UpdateTermCommandPayload data() {
        return new UpdateTermCommandPayload(Optional.of(before), Optional.empty());
    }

    @Override
    public DataType getDataType() {
        return DataType.TERM;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", before.id(), before.name(), ontologyId);
    }
}
