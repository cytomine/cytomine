//package be.cytomine.controller.security;
//
//import be.cytomine.CytomineCoreApplication;
//import be.cytomine.config.MongoTestConfiguration;
//import be.cytomine.config.PostGisTestConfiguration;
//import be.cytomine.dto.Account;
//import be.cytomine.dto.Accounts;
//import be.cytomine.service.security.AccountService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.tomakehurst.wiremock.WireMockServer;
//import com.nimbusds.jose.Algorithm;
//import com.nimbusds.jose.JOSEException;
//
//import com.nimbusds.jose.JWSAlgorithm;
//import com.nimbusds.jose.JWSHeader;
//import com.nimbusds.jose.crypto.RSASSASigner;
//import com.nimbusds.jose.jwk.KeyUse;
//import com.nimbusds.jose.jwk.RSAKey;
//import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
//
//import com.nimbusds.jwt.JWTClaimsSet;
//import com.nimbusds.jwt.SignedJWT;
//import io.jsonwebtoken.lang.Strings;
//import jakarta.ws.rs.core.Response;
//import org.junit.jupiter.api.*;
//
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.resource.RealmResource;
//import org.keycloak.admin.client.resource.UsersResource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
//import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//
//@SpringBootTest(classes = CytomineCoreApplication.class)
//@AutoConfigureMockMvc
//@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
//@WithMockUser(username = "admin")
//public class AccountTests {
//
//    @MockitoBean
//    private Keycloak keycloak;
//
//    @Autowired
//    private MockMvc allProtectedMockMvc;
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    ApplicationEventPublisher applicationEventPublisher;
//
//    private static final WireMockServer wireMockServer = new WireMockServer(8888);
//
//    private static RSAKey rsaKey;
//
//    private static final String KEY_ID = "some random string";
//
//    @BeforeEach
//    void setUp() {
//        // 1. Create the sub-resource mocks
//        RealmResource realmResource = mock(RealmResource.class);
//        UsersResource usersResource = mock(UsersResource.class);
//
//        // 2. Build the chain: keycloak.realm("...").users()
//        when(keycloak.realm(anyString())).thenReturn(realmResource);
//        when(realmResource.users()).thenReturn(usersResource);
//
//        // 3. Stub the actual network-heavy methods
//        // Mock successful user creation (HTTP 201)
//        when(usersResource.create(any())).thenReturn(Response.status(201).build());
//
//        // Mock user search to return an empty list (prevents "User Already Exists" errors)
//        when(usersResource.search(anyString())).thenReturn(Collections.emptyList());
//    }
//
//    @Test
//    public void whenNewAccountCreated_ok() throws Exception {
//        ObjectMapper objectMapper = new ObjectMapper();
//        Account account = createAccountForCreation();
//
//        allProtectedMockMvc.perform(post("/api/accounts")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + getSignedNotExpiredJwt())
//                        .content(objectMapper.writeValueAsString(account)))
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.reference").exists());
//    }
//
//    private static Account createAccountForCreation() {
//        Account account = new Account();
//        account.setUsername("test.user.name."+UUID.randomUUID());
//        account.setFirstName("test.first.name");
//        account.setLastName("test.last.name");
//        account.setEmail("test.user.email"+ Strings.replace(String.valueOf(UUID.randomUUID()), "-" , "")+"@gmail.com");
//        account.setPassword("password");
//        account.setDeveloper(true);
//        account.setUserLocale("en");
//        account.setRoles(List.of("ADMIN"));
//        return account;
//    }
//
//    @Test
//    public void whenAccountDeleted_noContent() throws Exception {
//        Account account = createAccountForDeletion();
//        ResponseEntity<?> responseEntity = accountService.createAccount(account);
//        Account body = (Account) responseEntity.getBody();
//
//        assert body != null;
//        allProtectedMockMvc.perform(delete("/api/accounts/{reference}", body.getReference()).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + getSignedNotExpiredJwt())).andExpect(status().isNoContent()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
//
//    }
//
//    @Test
//    public void whenAccountGot_ok() throws Exception {
//        Account account = createAccountForRetrieval();
//        ResponseEntity<?> responseEntity = accountService.createAccount(account);
//        Account body = (Account) responseEntity.getBody();
//
//        assert body != null;
//        allProtectedMockMvc.perform(get("/api/accounts/{reference}", body.getReference())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + getSignedNotExpiredJwt()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.first_name").value(account.getFirstName()))
//                .andExpect(jsonPath("$.last_name").value(account.getLastName()))
//                .andExpect(jsonPath("$.email").value(account.getEmail()));
//
//    }
//
//    private static Account createAccountForRetrieval() {
//        Account account = new Account();
//        account.setUsername("to.get.user.name." + UUID.randomUUID());
//        account.setFirstName("to.get.first.name");
//        account.setLastName("to.get.last.name");
//        account.setEmail("to.get.user.email"+ Strings.replace(String.valueOf(UUID.randomUUID()), "-" , "")+"@gmail.com");
//        account.setPassword("password");
//        account.setDeveloper(true);
//        account.setUserLocale("en");
//        account.setRoles(List.of("ADMIN"));
//        return account;
//    }
//
//    private static Account createAccountForDeletion() {
//        Account account = new Account();
//        account.setUsername("to.delete.user.name." + UUID.randomUUID());
//        account.setFirstName("to.delete.first.name");
//        account.setLastName("to.delete.last.name");
//        account.setEmail("to.delete.user.email"+ Strings.replace(String.valueOf(UUID.randomUUID()), "-" , "")+"@gmail.com");
//        account.setPassword("password");
//        account.setDeveloper(true);
//        account.setUserLocale("en");
//        account.setRoles(List.of("ADMIN"));
//        return account;
//    }
//
//    private void clean() {
//        ResponseEntity<?> accountsResponseEntity = accountService.find(0, 100);
//        Accounts accounts = (Accounts) accountsResponseEntity.getBody();
//        if (accounts != null) {
//            accounts.getItems().stream()
//                    .filter(accnt -> !accnt.getUsername().equalsIgnoreCase("admin"))
//                    .forEach(account -> {
//                        try {
//                            allProtectedMockMvc.perform(delete("/api/accounts/{reference}", account.getReference()).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + getSignedNotExpiredJwt())).andExpect(status().isNoContent()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    });
//        }
//    }
//
//    @Test
//    public void whenAccountUpdated_ok() throws Exception {
//        Account account = createAccountForUpdate();
//        ResponseEntity<?> responseEntity = accountService.createAccount(account);
//        Account body = (Account) responseEntity.getBody();
//
//        Assertions.assertNotNull(body);
//        Assertions.assertEquals(body.getUsername(), account.getUsername());
//        Assertions.assertEquals(body.getFirstName(), account.getFirstName());
//
//        account.setFirstName("to.updated.first.name");
//        account.setReference(body.getReference());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        allProtectedMockMvc.perform(
//                        put("/api/accounts/{reference}", account.getReference())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Authorization", "Bearer " + getSignedNotExpiredJwt())
//                                .content(objectMapper.writeValueAsString(account)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.first_name").value(account.getFirstName()));
//
//    }
//
//    @Test
//    public void whenPaginatedGet_ok() throws Exception {
//        Account account = createAccountForUpdate();
//        ResponseEntity<?> responseEntity = accountService.createAccount(account);
//        Account body = (Account) responseEntity.getBody();
//
//        Assertions.assertNotNull(body);
//        Assertions.assertEquals(body.getUsername(), account.getUsername());
//        Assertions.assertEquals(body.getFirstName(), account.getFirstName());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        allProtectedMockMvc.perform(
//                        get("/api/accounts", account.getReference())
//                                .param("offset", "0")
//                                .param("limit", "100")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Authorization", "Bearer " + getSignedNotExpiredJwt()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.items").exists())
//                .andExpect(jsonPath("$.total").isNotEmpty())
//                .andExpect(jsonPath("$.nbPages").value(1))
//                .andExpect(jsonPath("$.offset").value(0))
//                .andExpect(jsonPath("$.limit").value(100));
//    }
//
//    private static Account createAccountForUpdate() {
//        Account account = new Account();
//        account.setUsername("to.update.user.name."+UUID.randomUUID());
//        account.setFirstName("to.update.first.name");
//        account.setLastName("to.update.last.name");
//        account.setEmail("to.update.user.email"+ Strings.replace(String.valueOf(UUID.randomUUID()), "-" , "")+"@gmail.com");
//        account.setPassword("password");
//        account.setDeveloper(true);
//        account.setUserLocale("en");
//        account.setRoles(List.of("ADMIN"));
//        return account;
//    }
//
//    public static void configureWireMock(WireMockServer wireMockServer) throws JOSEException {
//        rsaKey = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).algorithm(new Algorithm("RS256")).keyID(KEY_ID).generate();
//
//        RSAKey rsaPublicJWK = rsaKey.toPublicJWK();
//        String jwkResponse = String.format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());
//
//        wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlMatching("/")).willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody(jwkResponse)));
//    }
//
//    @BeforeAll
//    public static void beforeAll() throws Exception {
//        configureWireMock(wireMockServer);
//        wireMockServer.start();
//    }
//
//    @AfterAll
//    public static void afterAll() {
//        try {
//            wireMockServer.stop();
//        } catch (Exception ignored) {
//        }
//    }
//
//    private String getSignedNotExpiredJwt() throws Exception {
//        return getSignedJwt();
//    }
//
//    private String getSignedJwt() throws Exception {
//
//        RSASSASigner signer = new RSASSASigner(rsaKey);
//        Instant issuedAt = Instant.now();
//        Map<String, Object> resourceAccessClaim = new HashMap<>();
//        Map<String, Object> resource = new HashMap<>();
//        List<String> resourceRoles = List.of("ADMIN");
//        resource.put("roles", resourceRoles);
//        resourceAccessClaim.put("core", resource);
//        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
//                .expirationTime(new Date(new Date().getTime() + 3600 * 1000))
//                .issuer("http://localhost:8888/")
//                .expirationTime(Date.from(Instant.now().plus(180, ChronoUnit.MINUTES)))
//                .issueTime(Date.from(issuedAt))
//                .claim("iss", "http://localhost:8888/")
//                .claim("sub", UUID.randomUUID())
//                .claim("name", "Some User")
//                .claim("preferred_username", UUID.randomUUID().toString())
//                .claim("resource_access", resourceAccessClaim)
//                .build();
//        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claimsSet);
//        signedJWT.sign(signer);
//        return signedJWT.serialize();
//    }
//
//}
