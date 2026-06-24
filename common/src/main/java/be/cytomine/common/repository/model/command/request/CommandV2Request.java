package be.cytomine.common.repository.model.command.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "commandType",
    visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = CreateTermCommand.class, name = "INSERT_TERM_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermCommand.class, name = "UPDATE_TERM_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = "DELETE_TERM_COMMAND"),
    @JsonSubTypes.Type(value = CreateTermRelationCommand.class, name = "INSERT_TERM_RELATION_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermRelationCommand.class, name = "UPDATE_TERM_RELATION_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermRelationCommand.class, name = "DELETE_TERM_RELATION_COMMAND"),
    @JsonSubTypes.Type(value = CreateOntologyCommand.class, name = "INSERT_ONTOLOGY_COMMAND"),
    @JsonSubTypes.Type(value = UpdateOntologyCommand.class, name = "UPDATE_ONTOLOGY_COMMAND"),
    @JsonSubTypes.Type(value = DeleteOntologyCommand.class, name = "DELETE_ONTOLOGY_COMMAND"),
    @JsonSubTypes.Type(value = CreateStorageCommand.class, name = "INSERT_STORAGE_COMMAND"),
    @JsonSubTypes.Type(value = UpdateStorageCommand.class, name = "UPDATE_STORAGE_COMMAND"),
    @JsonSubTypes.Type(value = DeleteStorageCommand.class, name = "DELETE_STORAGE_COMMAND"),
    @JsonSubTypes.Type(value = CreateUploadedFileCommand.class, name = "INSERT_UPLOADED_FILE_COMMAND"),
    @JsonSubTypes.Type(value = UpdateUploadedFileCommand.class, name = "UPDATE_UPLOADED_FILE_COMMAND"),
    @JsonSubTypes.Type(value = DeleteUploadedFileCommand.class, name = "DELETE_UPLOADED_FILE_COMMAND"),
    @JsonSubTypes.Type(value = CreateTagDomainAssociationCommand.class, name = "INSERT_TAG_DOMAIN_ASSOCIATION_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTagDomainAssociationCommand.class, name = "UPDATE_TAG_DOMAIN_ASSOCIATION_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTagDomainAssociationCommand.class, name = "DELETE_TAG_DOMAIN_ASSOCIATION_COMMAND")})
public sealed interface CommandV2Request<T extends HasLongId & HasAclId>
    permits DeleteCommandRequest, CreateCommandRequest, UpdateCommandRequest {

    UpdateCommandPayload<T> data();

    long userId();

    CommandType getCommandType();

    String getActionMessage();

    long id();

    String getCommand();
}
