package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record CreateTermCommand(TermCommandPayload after, long userId)
    implements CreateCommandRequest<TermCommandPayload> {


    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s) added in ontology %s", after.id(), after.name(), after.ontology());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_TERM;
    }
}
