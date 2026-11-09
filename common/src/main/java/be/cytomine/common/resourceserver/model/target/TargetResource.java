package be.cytomine.common.resourceserver.model.target;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.resourceserver.model.TargetResourceType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "targetResourceType",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InstanceTargetResource.class, name = "INSTANCE"),
    @JsonSubTypes.Type(value = ProjectTargetResource.class, name = "PROJECT"),
})
public sealed interface TargetResource permits InstanceTargetResource, ProjectTargetResource {
    TargetResourceType getResourceType();
}
