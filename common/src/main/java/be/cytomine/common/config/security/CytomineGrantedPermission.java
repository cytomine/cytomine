package be.cytomine.common.config.security;

import be.cytomine.common.config.security.target.TargetResource;

public record CytomineGrantedPermission(CytominePermission permission, TargetResource targetResource) {
}
