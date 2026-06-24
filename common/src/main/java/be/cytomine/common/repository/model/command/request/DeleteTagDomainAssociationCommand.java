package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagDomainAssociationCommandPayload;

import static java.lang.String.format;

public record DeleteTagDomainAssociationCommand(TagDomainAssociationCommandPayload before, long userId)
    implements DeleteCommandRequest<TagDomainAssociationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_TAG_DOMAIN_ASSOCIATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format(
            "TagDomainAssociation %s (tag=%s, domain=%s/%s) deleted",
            before.id(),
            before.tagId(),
            before.domainClassName(),
            before.domainId()
        );
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_TAG_DOMAIN_ASSOCIATION;
    }
}
