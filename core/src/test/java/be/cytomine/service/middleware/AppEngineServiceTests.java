package be.cytomine.service.middleware;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.service.appengine.AppEngineService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class AppEngineServiceTests {

    @Autowired
    private AppEngineService appEngineService;

    private static final WireMockServer wireMockServer = WiremockRepository.SERVER;

    @Value("${application.appEngine.apiBasePath}")
    private String apiBasePath;

    @Test
    void getTask() {
        configureFor("localhost", wireMockServer.port());
        stubFor(get(urlEqualTo(apiBasePath + "task"))
            .willReturn(
                aResponse().withBody("{\"a\":\"b\", \"c\":2}")
            )
        );
        String response = appEngineService.get("task");
        assertThat(response).isNotNull();
    }
}
