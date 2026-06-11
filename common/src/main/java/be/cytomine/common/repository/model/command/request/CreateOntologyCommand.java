package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;

import static java.lang.String.format;

public record CreateOntologyCommand(OntologyCommandPayload after, long userId)
    implements CreateCommandRequest<OntologyCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_ONTOLOGY_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Ontology created, with id %s and name %s", after.id(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_ONTOLOGY;
    }
}
