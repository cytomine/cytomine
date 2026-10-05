package org.cytomine.e2etests.utils;

import be.cytomine.common.repository.model.Role;

public record TestData(Role role,
                       String username,
                       String password,
                       String projectName,
                       String projectUrl,
                       String ontologyName,
                       String ontologyUrl) {
    public TestData {

    }
}
