package be.cytomine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
class CytomineCoreApplicationTests {

    @Test
    void contextLoads() {
    }
}
