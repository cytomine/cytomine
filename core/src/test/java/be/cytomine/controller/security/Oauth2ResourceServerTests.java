package be.cytomine.controller.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class Oauth2ResourceServerTests {

    private static final WireMockServer wireMockServer = new WireMockServer(8888);
    private static final String KEY_ID = "some random string";
    private static RSAKey rsaKey;
    @Autowired
    private MockMvc allProtectedMockMvc;

    public static void configureWireMock(WireMockServer wireMockServer) throws JOSEException {
        rsaKey = new RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(new Algorithm("RS256"))
            .keyID(KEY_ID)
            .generate();

        RSAKey rsaPublicJWK = rsaKey.toPublicJWK();
        String jwkResponse = String.format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());

        wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlMatching("/"))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));
    }

    @BeforeAll
    public static void beforeAll() throws JOSEException {
        configureWireMock(wireMockServer);
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    public void whenNoTokenProvidedThenUnauthorized() throws Exception {
        allProtectedMockMvc.perform(get("/api/project/45.json"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenInvalidTokenProvidedThenUnauthorized() throws Exception {
        allProtectedMockMvc.perform(get("/api/project/45.json")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("Randomly fails")
    public void whenValidTokenProvidedThenNotFoundAsOk() throws Exception {
        // get a valid cytomine access token using password grant from iam microservice
        allProtectedMockMvc.perform(get("/api/project/45.json")
                .header("Authorization", "Bearer " + getSignedNotExpiredJwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    public void whenExpiredTokenProvidedThenUnauthorized() throws Exception {
        // get a valid cytomine access token using password grant from iam microservice
        allProtectedMockMvc.perform(get("/api/project/45.json")
                .header("Authorization", "Bearer " + getSignedExpiredJwt()))
            .andExpect(status().isUnauthorized());
    }

    private String getSignedNotExpiredJwt() throws Exception {
        return getSignedJwt(Instant.now().plus(10, ChronoUnit.MINUTES));
    }

    private String getSignedExpiredJwt() throws Exception {
        return getSignedJwt(Instant.now().minus(10, ChronoUnit.MINUTES));
    }

    private String getSignedJwt(Instant expiresAt) throws Exception {

        RSASSASigner signer = new RSASSASigner(rsaKey);
        Instant issuedAt = Instant.now();
        Map<String, Object> resourceAccessClaim = new HashMap<>();
        Map<String, Object> resource = new HashMap<>();
        List<String> resourceRoles = List.of("ADMIN");
        resource.put("roles", resourceRoles);
        resourceAccessClaim.put("core", resource);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .issuer("http://localhost:8888/")
            .expirationTime(Date.from(expiresAt))
            .issueTime(Date.from(issuedAt))
            .claim("iss", "http://localhost:8888/")
            .claim("sub", UUID.randomUUID())
            .claim("name", "Some User")
            .claim("preferred_username", "test_user_from_token")
            .claim("resource_access", resourceAccessClaim)
            .build();
        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.getKeyID())
                .build(), claimsSet
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

}
