package be.cytomine.common.resourceserver.model;

import be.cytomine.common.resourceserver.model.target.TargetResource;

public record CytomineRequestedPermission(CytominePermission permission, TargetResource targetResource) {
}
