package be.cytomine.common.config.security;

import be.cytomine.common.config.security.target.TargetResource;

public record CytomineRequestedPermission(CytominePermission permission, TargetResource targetResource) {
}
