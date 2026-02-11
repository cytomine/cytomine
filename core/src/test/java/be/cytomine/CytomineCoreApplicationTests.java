package be.cytomine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.PostGisTestConfiguration;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
class CytomineCoreApplicationTests {

    @Test
    void contextLoads() {
    }
}
