package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;

import static java.lang.String.format;

public record DeleteOntologyCommand(OntologyCommandPayload before, long userId)
    implements DeleteCommandRequest<OntologyCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_ONTOLOGY_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Ontology deleted, with id %s and name %s", before.id(), before.name());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_ONTOLOGY;
    }
}
