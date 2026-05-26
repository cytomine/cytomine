package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record DeleteTermCommand(Long id, TermCommandPayload before, long userId, long ontologyId)
    implements DeleteCommandRequest<TermCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) deleted in ontology %s", before.id(), before.name(), ontologyId);
    }
}
