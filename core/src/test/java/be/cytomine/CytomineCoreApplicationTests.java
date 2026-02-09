package be.cytomine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import be.cytomine.integration.config.PostGisConfiguration;

@Import({PostGisConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
class CytomineCoreApplicationTests {

    @Test
    void contextLoads() {
    }
}
