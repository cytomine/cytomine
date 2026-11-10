package be.cytomine.common.resourceserver.model.permissionrequest;

import be.cytomine.common.resourceserver.model.permission.CytomineProjectPermission;
import be.cytomine.common.resourceserver.model.target.ProjectTargetResource;

public record CytomineProjectRequestedPermission(CytomineProjectPermission permission, ProjectTargetResource target)
    implements CytomineRequestedPermission {
}
