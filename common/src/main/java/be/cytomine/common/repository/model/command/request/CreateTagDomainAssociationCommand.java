package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagDomainAssociationCommandPayload;

import static java.lang.String.format;

public record CreateTagDomainAssociationCommand(TagDomainAssociationCommandPayload after, long userId)
    implements CreateCommandRequest<TagDomainAssociationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_TAG_DOMAIN_ASSOCIATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("TagDomainAssociation %s (tag=%s, domain=%s/%s) added",
            after.id(), after.tagId(), after.domainClassName(), after.domainId());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_TAG_DOMAIN_ASSOCIATION;
    }
}
