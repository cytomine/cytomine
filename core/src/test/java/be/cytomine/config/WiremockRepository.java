package be.cytomine.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.web.util.UriComponentsBuilder;

import be.cytomine.TermMapper;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.security.User;
import be.cytomine.dto.appengine.task.TaskRunProvisionedResponse;

import static be.cytomine.BasicInstanceBuilder.ROLE_ADMIN;
import static be.cytomine.BasicInstanceBuilder.ROLE_GUEST;
import static be.cytomine.BasicInstanceBuilder.ROLE_SUPER_ADMIN;
import static be.cytomine.BasicInstanceBuilder.ROLE_USER;
import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@TestConfiguration(proxyBeanMethods = false)
public class WiremockRepository {

    public static final WireMockServer SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    private static final LocalDateTime CREATION_DATE = LocalDateTime.of(2024, 1, 1, 0, 0);

    static {
        SERVER.start();
        setupStubs();
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TermMapper termMapper;

    @Value("${application.appEngine.apiBasePath}")
    private String apiBasePath;

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

        SERVER.stubFor(post(urlPathEqualTo(CBIR_API_BASE_PATH + "/storages"))
            .withRequestBody(matching(".*"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        SERVER.stubFor(WireMock.put(urlPathMatching("/reviewed-annotations/terms/.*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("[]")
            )
        );

        SERVER.stubFor(WireMock.get(urlPathMatching("/terms/ontology/.*/all-terms"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("[]")
            )
        );

        SERVER.stubFor(WireMock.get(urlPathMatching("/term_relations/term/.*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("[]")
            )
        );

        SERVER.stubFor(WireMock.get(urlPathMatching("/term_relations/ontology/.*/ids"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("[]")
            )
        );

        SERVER.stubFor(WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/storages"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(UUID.randomUUID().toString())
            )
        );

        SERVER.stubFor(WireMock.get(urlPathEqualTo("/users/search/ImageServer1"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "dataType": "USER",
                      "id": 1,
                      "username": "ImageServer1",
                      "email": "imageserver@cytomine.local",
                      "name": "Image Server",
                      "lastname": null,
                      "firstname": null,
                      "language": null,
                      "isDeveloper": false,
                      "origin": null,
                      "updated": null,
                      "deleted": null,
                      "created": "2024-01-01T00:00:00",
                      "privateKey": "imageServerPrivateKey",
                      "publicKey": "imageServerPublicKey",
                      "roles": []
                    }
                    """)
            )
        );

        SERVER.stubFor(WireMock.get(urlPathMatching("/users/search/.*"))
            .atPriority(10)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("null")
            )
        );

        SERVER.stubFor(WireMock.get(urlPathMatching("/users/\\d+"))
            .atPriority(10)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("null")
            )
        );

        SERVER.stubFor(WireMock.put(urlPathMatching("/users/\\d+"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("null")
            )
        );
    }

    @PostConstruct
    public void setupUserStubs() {
        stubTestUser("SUPER_ADMIN_ACL", 1001, ROLE_SUPER_ADMIN);
        stubTestUser("ADMIN_ACL", 1002, ROLE_ADMIN);
        stubTestUser("ACL_USER_NO_ACL", 1003, ROLE_USER);
        stubTestUser("USER_ACL_READ", 1004, ROLE_USER);
        stubTestUser("USER_ACL_WRITE", 1005, ROLE_USER);
        stubTestUser("USER_ACL_CREATE", 1006, ROLE_USER);
        stubTestUser("USER_ACL_DELETE", 1007, ROLE_USER);
        stubTestUser("USER_ACL_ADMIN", 1008, ROLE_USER);
        stubTestUser("CREATOR", 1009, ROLE_USER);
        stubTestUser("GUEST_ACL", 1010, ROLE_GUEST);
    }

    private void stubTestUser(String username, long id, String role) {
        stubUser(id, username, role, username + "-public-key", username + "-private-key");
    }

    public void stubUser(User user) {
        stubUser(user.getId(), user.getUsername(), ROLE_USER, user.getPublicKey(), user.getPrivateKey());
    }

    @SneakyThrows
    private void stubUser(long id, String username, String role, String publicKey, String privateKey) {
        UserResponse user = new UserResponse(
            id,
            username,
            username.toLowerCase() + "@test.cytomine.local",
            Optional.of("firstname lastname"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            CREATION_DATE,
            Optional.ofNullable(privateKey),
            Optional.ofNullable(publicKey),
            rolesOf(role)
        );
        String body = objectMapper.writeValueAsString(user);

        SERVER.stubFor(WireMock.get(urlPathEqualTo("/users/search/" + username))
            .atPriority(2)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(body)
            )
        );

        SERVER.stubFor(WireMock.get(urlPathEqualTo("/users/" + id))
            .atPriority(2)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(body)
            )
        );
    }

    private Set<RoleResponse> rolesOf(String role) {
        List<String> authorities = switch (role) {
            case ROLE_SUPER_ADMIN -> List.of(ROLE_SUPER_ADMIN, ROLE_ADMIN, ROLE_USER);
            case ROLE_ADMIN -> List.of(ROLE_ADMIN, ROLE_USER);
            default -> List.of(role);
        };

        return IntStream.range(0, authorities.size())
            .mapToObj(index -> new RoleResponse(
                index + 1L, authorities.get(index), CREATION_DATE, Optional.empty(), Optional.empty()))
            .collect(Collectors.toSet());
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
    public void stubTermsByProject(long projectId, Term term) {
        SERVER.stubFor(WireMock.get(urlPathEqualTo("/terms/project/" + projectId))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Map.of(
                    "content", List.of(termMapper.map(term)),
                    "number", 0,
                    "size", 1,
                    "totalElements", 1
                )))
            )
        );
    }

    @SneakyThrows
    public void stubProvisionParameter(TaskRunProvisionedResponse response) {
        String urlPath = UriComponentsBuilder.fromPath(apiBasePath)
            .pathSegment("task-runs", response.taskRunId().toString(), "input-provisions", response.parameterName())
            .toUriString();

        SERVER.stubFor(WireMock.put(urlPathEqualTo(urlPath))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response))
            )
        );
    }
}
