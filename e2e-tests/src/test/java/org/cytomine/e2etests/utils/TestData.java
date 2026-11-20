package org.cytomine.e2etests.utils;

import org.cytomine.common.repository.model.Role;

public record TestData(Role role,
                       String username,
                       String password,
                       String projectName,
                       String projectUrl,
                       String ontologyName,
                       String ontologyUrl) {
}
