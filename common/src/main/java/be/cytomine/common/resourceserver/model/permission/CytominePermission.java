package be.cytomine.common.resourceserver.model.permission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "permissionType",
    visible = false)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CytomineInstancePermission.class, name = "INSTANCE"),
    @JsonSubTypes.Type(value = CytomineProjectPermission.class,name = "PROJECT"),
})
public sealed interface CytominePermission permits CytomineInstancePermission, CytomineProjectPermission {
    PermissionType getPermissionType();
}
