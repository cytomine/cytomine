package be.cytomine.common.resourceserver.model;

import be.cytomine.common.resourceserver.model.target.TargetResource;

public record CytomineDeniedPermissions(CytominePermission permission, TargetResource targetResource) {
}
