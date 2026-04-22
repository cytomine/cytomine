package be.cytomine.config;

import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistrar;

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
}
