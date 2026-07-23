package be.cytomine.common.repository.model.command.payload.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.HasLocaleDateTimeCUD;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.DataType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "dataType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OntologyResponse.class, name = "ONTOLOGY"),
    @JsonSubTypes.Type(value = RoleResponse.class, name = "ROLE"),
    @JsonSubTypes.Type(value = StorageResponse.class, name = "STORAGE"),
    @JsonSubTypes.Type(value = TagResponse.class, name = "TAG"),
    @JsonSubTypes.Type(value = TagDomainAssociationResponse.class, name = "TAG_DOMAIN_ASSOCIATION"),
    @JsonSubTypes.Type(value = TermResponse.class, name = "TERM"),
    @JsonSubTypes.Type(value = TermRelationResponse.class, name = "TERM_RELATION"),
    @JsonSubTypes.Type(value = UploadedFileResponse.class, name = "UPLOADED_FILE"),
    @JsonSubTypes.Type(value = UndoCommandResponse.class, name = "UNDO_COMMAND"),
    @JsonSubTypes.Type(value = UserResponse.class, name = "USER"),
    @JsonSubTypes.Type(value = UserRoleResponse.class, name = "USER_ROLE"),
})
public sealed interface ApplyCommandResponse extends HasLongId, HasLocaleDateTimeCUD
    permits OntologyResponse,
    RoleResponse,
    StorageResponse,
    TagResponse,
    TagDomainAssociationResponse,
    TermRelationResponse,
    TermResponse,
    UndoCommandResponse,
    UploadedFileResponse,
    UserResponse,
    UserRoleResponse {
    DataType getDataType();
}
