package org.cytomine.keycloak;

import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

class AppTest {
    @Test
    void appHasAGreeting() {
        CytomineProtocolMapperApp classUnderTest = new CytomineProtocolMapperApp();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
