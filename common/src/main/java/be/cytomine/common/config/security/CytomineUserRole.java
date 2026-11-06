package be.cytomine.common.config.security;

import java.util.Set;

public record CytomineUserRole(String name, Set<String> roles) {
}
