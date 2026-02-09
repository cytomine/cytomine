package be.cytomine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.PostGisTestConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
class CytomineCoreApplicationTests {

    @Test
    void contextLoads() {
    }
}
