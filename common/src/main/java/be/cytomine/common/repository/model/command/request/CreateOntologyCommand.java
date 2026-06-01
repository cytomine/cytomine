package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;

public record CreateOntologyCommand(OntologyCommandPayload after, long userId)
    implements CreateCommandRequest<OntologyCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_ONTOLOGY_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return "";
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_ONTOLOGY;
    }
}
