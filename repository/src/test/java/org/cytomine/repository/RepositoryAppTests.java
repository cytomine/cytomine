package org.cytomine.repository;

import org.cytomine.common.PostGisTestConfiguration;
import org.cytomine.repository.http.SecurityMockMvcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({PostGisTestConfiguration.class, SecurityMockMvcTestConfiguration.class})
class RepositoryAppTests {

    @Test
    void contextLoads() {
    }

}
