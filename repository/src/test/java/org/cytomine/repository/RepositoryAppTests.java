package org.cytomine.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.SecurityMockMvcTestConfiguration;

@SpringBootTest
@Import({PostGisTestConfiguration.class, SecurityMockMvcTestConfiguration.class})
class RepositoryAppTests {

    @Test
    void contextLoads() {
    }

}
