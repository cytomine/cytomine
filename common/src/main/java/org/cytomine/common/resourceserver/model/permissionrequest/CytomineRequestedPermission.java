package org.cytomine.common.resourceserver.model.permissionrequest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.cytomine.common.resourceserver.model.permission.CytominePermission;
import org.cytomine.common.resourceserver.model.permission.PermissionType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "permissionType",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CytomineInstanceRequestedPermission.class, name = "INSTANCE"),
    @JsonSubTypes.Type(value = CytomineProjectRequestedPermission.class, name = "PROJECT"),
})
public sealed interface CytomineRequestedPermission
    permits CytomineInstanceRequestedPermission, CytomineProjectRequestedPermission {

    PermissionType permissionType();
    CytominePermission permission();

}
