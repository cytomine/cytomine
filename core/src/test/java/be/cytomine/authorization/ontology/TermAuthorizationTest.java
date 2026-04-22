package be.cytomine.authorization.ontology;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.TermMapper;
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.domain.ontology.Term;
import be.cytomine.service.ontology.TermService;

import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class TermAuthorizationTest extends CRDAuthorizationTest {
    private static final WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        setupStub();
    }

    @Autowired
    TermService termService;
    @Autowired
    BasicInstanceBuilder builder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;
    @Autowired
    private TermMapper termMapper;
    private Term term = null;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("application.repositoryURL", () -> "http://localhost:" + wireMockServer.port());
    }

    private static void setupStub() {
        /* Simulate call to PIMS */
        wireMockServer.stubFor(WireMock.post(urlPathMatching(IMS_API_BASE_PATH + "/image/.*/annotation/drawing"))
            .withRequestBody(WireMock.matching(".*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(UUID.randomUUID().toString().getBytes())
            )
        );

        /* Simulate call to CBIR server */
        wireMockServer.stubFor(WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/images"))
            .withQueryParam("storage", matching(".*"))
            .withQueryParam("index", equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @SneakyThrows
    @BeforeEach
    public void beforeEach() throws ParseException {
        if (term == null) {
            term = builder.givenATerm();
            initACL(term.container());
        }

        wireMockServer.stubFor(WireMock.get(urlPathMatching("/terms/" + term.getId()))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(termMapper.map(term)))
            )
        );


    }

    @Override
    public void whenIGetDomain() {
        termService.get(term.getId());
    }

    @Override
    protected void whenIAddDomain() {
        termService.add(
            basicInstanceBuilder.givenANotPersistedTerm(term.getOntology()).toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Term termToDelete = builder.givenATerm(term.getOntology());
        termService.delete(termToDelete, null, null, true);
    }

    @Test
    @Disabled
    @Override
    public void guestAddDomain() {
    }

    @Test
    @Disabled
    @Override
    public void userWithoutPermissionAddDomain() {
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.DELETE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.empty();
    }

    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.empty();
    }
}
