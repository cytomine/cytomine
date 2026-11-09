package be.cytomine.common.config.security;

import be.cytomine.common.config.security.target.TargetResource;

public record CytomineDeniedPermissions(CytominePermission permission, TargetResource targetResource) {
}
