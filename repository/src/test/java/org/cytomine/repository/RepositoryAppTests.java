package org.cytomine.repository;

import be.cytomine.common.PostGisTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostGisTestConfiguration.class)
class RepositoryAppTests {

    @Test
    void contextLoads() {
    }
}
