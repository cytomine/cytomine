package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record CreateTermCommand(TermCommandPayload after, long userId, long ontologyId)
    implements CreateCommandRequest<TermCommandPayload> {


    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", after.id(), after.name(), ontologyId);
    }
}
