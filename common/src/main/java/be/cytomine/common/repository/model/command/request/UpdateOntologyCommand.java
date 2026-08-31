package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;

import static java.lang.String.format;

public record UpdateOntologyCommand(OntologyCommandPayload before, OntologyCommandPayload after, long userId)
    implements UpdateCommandRequest<OntologyCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_ONTOLOGY_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Ontology %s updated, (name: %s => %s)", after.id(), before.name(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_ONTOLOGY;
    }
}
