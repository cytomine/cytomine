package org.cytomine.keycloak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {
    @Test
    void appHasAGreeting() {
        CytomineProtocolMapperApp classUnderTest = new CytomineProtocolMapperApp();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
