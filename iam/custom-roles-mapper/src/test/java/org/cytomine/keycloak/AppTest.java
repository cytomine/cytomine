
package org.cytomine.keycloak;

import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test void appHasAGreeting() {
        CustomRolesMapper classUnderTest = new CustomRolesMapper();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
