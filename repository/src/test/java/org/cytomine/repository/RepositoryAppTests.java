package org.cytomine.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.common.PostGisTestConfiguration;

@SpringBootTest
@Import(PostGisTestConfiguration.class)
class RepositoryAppTests {

    @Test
    void contextLoads() {
    }
}
