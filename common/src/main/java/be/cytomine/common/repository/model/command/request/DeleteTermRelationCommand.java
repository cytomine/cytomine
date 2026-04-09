package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;

import static java.lang.String.format;

public record DeleteTermRelationCommand(Long id, TermRelationCommandPayload before, long userId, long ontologyId)
    implements DeleteCommandRequest<TermRelationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_TERM_RELATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Term relation %s (term1: %s, term2: %s) deleted in ontology %s", before.id(), before.term1Id(),
            before.term2Id(), ontologyId);
    }
}
