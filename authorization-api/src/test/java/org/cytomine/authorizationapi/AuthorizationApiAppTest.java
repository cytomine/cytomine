package org.cytomine.authorizationapi;

import org.cytomine.common.PostGisTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({PostGisTestConfiguration.class})
@SpringBootTest
class AuthorizationApiAppTest {
    @Test
    void contextLoads() {
    }
}
