package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagDomainAssociationCommandPayload;

import static java.lang.String.format;

public record UpdateTagDomainAssociationCommand(
    TagDomainAssociationCommandPayload before,
    TagDomainAssociationCommandPayload after,
    long userId
) implements UpdateCommandRequest<TagDomainAssociationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_TAG_DOMAIN_ASSOCIATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("TagDomainAssociation %s updated (tag: %s => %s, domain: %s/%s => %s/%s)",
            before.id(),
            before.tagId(), after.tagId(),
            before.domainClassName(), before.domainId(),
            after.domainClassName(), after.domainId());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_TAG_DOMAIN_ASSOCIATION;
    }
}
