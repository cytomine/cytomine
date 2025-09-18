package be.cytomine.appengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.appengine.config.HasPostgres;

@Import(HasPostgres.class)
@SpringBootTest
class AppEngineApplicationTests {
    @Test
    void contextLoads() {
    }
}
