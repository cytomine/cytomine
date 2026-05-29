package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;

public record DeleteOntologyCommand(Long id, OntologyCommandPayload before, long userId)
    implements DeleteCommandRequest<OntologyCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_ONTOLOGY_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return "";
    }
}
