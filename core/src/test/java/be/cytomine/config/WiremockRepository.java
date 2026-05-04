package be.cytomine.config;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistrar;

import be.cytomine.TermMapper;
import be.cytomine.domain.ontology.Term;
import be.cytomine.dto.appengine.task.TaskRunProvisionedResponse;

import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@TestConfiguration(proxyBeanMethods = false)
public class WiremockRepository {

    public static final WireMockServer SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        SERVER.start();
        setupStubs();
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TermMapper termMapper;

    public static void setupStubs() {
        SERVER.stubFor(WireMock.post(urlPathMatching(IMS_API_BASE_PATH + "/image/.*/annotation/drawing"))
            .withRequestBody(WireMock.matching(".*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(UUID.randomUUID().toString().getBytes())
            )
        );

        SERVER.stubFor(WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/images"))
            .withQueryParam("storage", matching(".*"))
            .withQueryParam("index", equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        SERVER.stubFor(WireMock.delete(urlPathMatching(CBIR_API_BASE_PATH + "/images/.*"))
            .withQueryParam("storage", matching(".*"))
            .withQueryParam("index", equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        SERVER.stubFor(WireMock.put(urlPathMatching("/reviewed-annotations/terms/.*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("[]")
            )
        );
    }

    @Bean
    public WireMockServer wireMockServer() {
        return SERVER;
    }

    @Bean
    public DynamicPropertyRegistrar wiremockProperties() {
        return registry -> {
            registry.add("application.appEngine.apiUrl", () -> "http://localhost:" + SERVER.port());
            registry.add("application.cbirURL", () -> "http://localhost:" + SERVER.port());
            registry.add("application.pimsURL", () -> "http://localhost:" + SERVER.port());
            registry.add("application.repositoryURL", () -> "http://localhost:" + SERVER.port());
        };
    }

    @SneakyThrows
    public void stubTerm(Term term) {
        SERVER.stubFor(WireMock.get(urlPathMatching("/terms/" + term.getId()))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(termMapper.map(term)))
            )
        );
    }

    @SneakyThrows
    public void stubProvisionParameter(TaskRunProvisionedResponse response) {
        SERVER.stubFor(WireMock.get(urlPathMatching("/task-runs/"
                + response.taskRunId()
                + "/input-provisions/"
                + response.parameterName()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response))
            )
        );
    }
}
