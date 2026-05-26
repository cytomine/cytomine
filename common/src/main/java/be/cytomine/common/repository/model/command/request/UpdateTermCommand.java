package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;

import static java.lang.String.format;

public record UpdateTermCommand(Long id, TermCommandPayload before, TermCommandPayload after, long userId,
                                Long ontologyId)
    implements UpdateCommandRequest<TermCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Term %s (%s => %s),color (%s => %s) updated in ontology %s", before.id(), before.name(),
            after.name(), before.color(), after.color(), ontologyId);
    }
}
