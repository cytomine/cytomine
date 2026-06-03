package org.cytomine.repository.http;

import java.util.List;
import java.util.Optional;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;
import be.cytomine.common.repository.utils.SpringPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class, properties = "spring.datasource.url: jdbc:tc:postgres")
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
class TermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private CommandV2Repository commandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long ontologyId;
    private Long projectId;
    private Long userId;
    private Long adminRoleId;

    @BeforeEach
    void setUp() {
        userId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);

        // Make user an admin
        adminRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_role (id, version, authority) VALUES (?, 0, 'ROLE_ADMIN')", adminRoleId);
        Long userRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) VALUES (?, 0, ?, ?)",
            userRoleId, userId, adminRoleId);

        ontologyId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, 'test', ?)", ontologyId,
            userId);

        projectId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("""
            INSERT INTO project (id, version, name, ontology_id, mode,
                are_images_downloadable, blind_mode, count_annotations, count_images,
                count_job_annotations, count_reviewed_annotations, hide_admins_layers,
                hide_users_layers, is_closed)
            VALUES (?, 0, 'test', ?, 'CLASSIC', false, false, 0, 0, 0, 0, false, false, false)
            """, projectId, ontologyId);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM command_v2");
        jdbcTemplate.update("DELETE FROM sec_user_sec_role WHERE sec_user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM sec_role WHERE id = ?", adminRoleId);  // make it a field
        jdbcTemplate.update("DELETE FROM term WHERE ontology_id = ?", ontologyId);
        jdbcTemplate.update("DELETE FROM project WHERE id = ?", projectId);
        jdbcTemplate.update("DELETE FROM ontology WHERE id = ?", ontologyId);
        jdbcTemplate.update("DELETE FROM sec_user WHERE id = ?", userId);
    }

    @Test
    @SneakyThrows
    void findTermByIdWhenExistsReturnsTerm() {
        TermResponse entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/{id}", entity.id()).param("userId", userId.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        TermResponse result = objectMapper.readValue(response, TermResponse.class);
        TermResponse expected =
            new TermResponse(entity.id(), "term1", "#FF0000", ontologyId, entity.created(), entity.updated(),
                Optional.empty(), Optional.empty(), null);
        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void findTermByIdWhenNotExistsReturnsEmpty() {
        mockMvc.perform(get("/terms/{id}", 999L).param("userId", userId.toString())).andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsTerm() {
        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);

        String response = mockMvc.perform(
                post("/terms").param("userId", userId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTerm))).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response, HttpCommandResponse.class);

        TermResponse dataResult = (TermResponse) result.data();

        assertEquals("newTerm", dataResult.name());
        assertEquals("#00FF00", dataResult.color());
        assertEquals(ontologyId, dataResult.ontologyId());

        CommandV2Entity command = commandRepository.findById(result.commandId()).orElseThrow();
        assertEquals(CommandType.INSERT_TERM_COMMAND, command.getData().getCommandType());
        assertEquals(userId, command.getUserId());


    }

    @Test
    @SneakyThrows
    void updateWhenExistsUpdatesAndReturnsTerm() {
        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);
        String createResponse = mockMvc.perform(
                post("/terms").param("userId", userId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTerm))).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();
        Long resultId = ((TermResponse) objectMapper.readValue(createResponse, HttpCommandResponse.class).data()).id();

        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.of("#00FF00"));

        String response = mockMvc.perform(
                put("/terms/{id}", resultId).param("userId", userId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTerm))).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response, HttpCommandResponse.class);
        TermResponse dataResult = (TermResponse) result.data();
        assertEquals("newName", dataResult.name());
        assertEquals("#00FF00", dataResult.color());

        CommandV2Entity command = commandRepository.findById(result.commandId()).orElseThrow();
        assertEquals(userId, command.getUserId());

    }

    @Test
    @SneakyThrows
    void updateWhenNotExistsReturnsEmpty() {
        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.empty());

        mockMvc.perform(
                put("/terms/{id}", 999L).param("userId", userId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTerm))).andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsDeletesAndReturnsTerm() {
        TermResponse entity = createAndSaveTermEntity("term1", "#FF0000");

        assertTrue(termRepository.findByIdAndDeletedNull(entity.id()).isPresent());

        String response = mockMvc.perform(delete("/terms/{id}", entity.id()).param("userId", userId.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response, HttpCommandResponse.class);
        TermResponse dataResult = (TermResponse) result.data();
        assertEquals("term1", dataResult.name());
        assertTrue(termRepository.findByIdAndDeletedNull(entity.id()).isEmpty());

        CommandV2Entity command = commandRepository.findById(result.commandId()).orElseThrow();
        assertEquals(CommandType.DELETE_TERM_COMMAND, command.getData().getCommandType());
        assertEquals(userId, command.getUserId());

    }

    @Test
    @SneakyThrows
    void findTermsByProjectReturnsPageOfTerms() {
        TermResponse entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/project/{id}", projectId).param("userId", userId.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        SpringPage<TermResponse> page = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(1, page.getTotalElements());
        TermResponse expected =
            new TermResponse(entity.id(), "term1", "#FF0000", ontologyId, entity.created(), entity.updated(),
                Optional.empty(), Optional.empty(), null);
        assertEquals(List.of(expected), page.getContent());
    }

    @Test
    @SneakyThrows
    void findTermsByOntologyReturnsPageOfTerms() {
        TermResponse entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/ontology/{id}", ontologyId).param("userId", userId.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        SpringPage<TermResponse> page = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(1, page.getTotalElements());
        TermResponse expected =
            new TermResponse(entity.id(), "term1", "#FF0000", ontologyId, entity.created(), entity.updated(),
                Optional.empty(), Optional.empty(), null);
        assertEquals(List.of(expected), page.getContent());
    }

    @Test
    @SneakyThrows
    void createWhenUserHasNoWriteAccessReturnsEmpty() {
        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);

        mockMvc.perform(
                post("/terms").param("userId", nonAdminUserId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTerm))).andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertTrue(termRepository.findAll().isEmpty());
    }

    @SneakyThrows
    private TermResponse createAndSaveTermEntity(String name, String color) {

        CreateTerm createTerm = new CreateTerm(name, color, ontologyId, Optional.empty());

        String createResponse = mockMvc.perform(
                post("/terms").param("userId", userId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTerm))).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();
        return (TermResponse) objectMapper.readValue(createResponse, HttpCommandResponse.class).data();
    }
}
