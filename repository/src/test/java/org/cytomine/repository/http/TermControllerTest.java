package org.cytomine.repository.http;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.SpringPage;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class TermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long ontologyId;
    private Long projectId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);

        // Make user an admin
        Long adminRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_role (id, version, authority) VALUES (?, 0, 'ROLE_ADMIN')", adminRoleId);
        Long userRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) VALUES (?, 0, ?, ?)",
            userRoleId, userId, adminRoleId);

        ontologyId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, 'test', ?)",
            ontologyId, userId);

        projectId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("""
            INSERT INTO project (id, version, name, ontology_id, mode,
                are_images_downloadable, blind_mode, count_annotations, count_images,
                count_job_annotations, count_reviewed_annotations, hide_admins_layers,
                hide_users_layers, is_closed)
            VALUES (?, 0, 'test', ?, 'CLASSIC', false, false, 0, 0, 0, 0, false, false, false)
            """, projectId, ontologyId);
    }

    @Test
    @SneakyThrows
    void findTermByIdWhenExistsReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/{id}", entity.getId()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        TermResponse result = objectMapper.readValue(response, TermResponse.class);
        TermResponse expected = new TermResponse(entity.getId(), "term1", "#FF0000", ontologyId,
            entity.getCreated(), entity.getUpdated(), "", null);
        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void findTermByIdWhenNotExistsReturnsEmpty() {
        mockMvc.perform(get("/terms/{id}", 999L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    void findAllReturnsPageOfTerms() {
        TermEntity entity1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity entity2 = createAndSaveTermEntity("term2", "#00FF00");

        String response = mockMvc.perform(get("/terms"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        SpringPage<TermResponse> page = objectMapper.readValue(response,
            new TypeReference<SpringPage<TermResponse>>() {});

        assertEquals(2, page.getTotalElements());
        TermResponse expected1 = new TermResponse(entity1.getId(), "term1", "#FF0000", ontologyId,
            entity1.getCreated(), entity1.getUpdated(), "", null);
        TermResponse expected2 = new TermResponse(entity2.getId(), "term2", "#00FF00", ontologyId,
            entity2.getCreated(), entity2.getUpdated(), "", null);
        assertEquals(List.of(expected1, expected2), page.getContent());
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsTerm() {
        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);

        String response = mockMvc.perform(post("/terms")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTerm)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse<TermResponse> result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructParametricType(HttpCommandResponse.class, TermResponse.class));

        assertEquals("newTerm", result.data().name());
        assertEquals("#00FF00", result.data().color());
        assertEquals(ontologyId, result.data().ontologyId());

        CommandEntity command = commandRepository.findById(result.command()).orElseThrow();
        assertEquals("be.cytomine.domain.command.AddCommand", command.getCommandType());
        assertEquals(userId, command.getUserId());
        assertEquals("TermService", command.getServiceName());
        assertTrue(command.isSaveOnUndoRedoStack());
    }

    @Test
    @SneakyThrows
    void updateWhenExistsUpdatesAndReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("oldName", "#FF0000");
        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.of("#00FF00"));

        String response = mockMvc.perform(put("/terms/{id}", entity.getId())
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTerm)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse<TermResponse> result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructParametricType(HttpCommandResponse.class, TermResponse.class));

        assertEquals("newName", result.data().name());
        assertEquals("#00FF00", result.data().color());

        CommandEntity command = commandRepository.findById(result.command()).orElseThrow();
        assertEquals("be.cytomine.domain.command.EditCommand", command.getCommandType());
        assertEquals(userId, command.getUserId());
        assertEquals("TermService", command.getServiceName());
        assertTrue(command.isSaveOnUndoRedoStack());
    }

    @Test
    @SneakyThrows
    void updateWhenNotExistsReturnsEmpty() {
        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.empty());

        mockMvc.perform(put("/terms/{id}", 999L)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTerm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsDeletesAndReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(delete("/terms/{id}", entity.getId())
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse<TermResponse> result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructParametricType(HttpCommandResponse.class, TermResponse.class));

        assertEquals("term1", result.data().name());
        assertTrue(termRepository.findById(entity.getId()).isEmpty());

        CommandEntity command = commandRepository.findById(result.command()).orElseThrow();
        assertEquals("be.cytomine.domain.command.DeleteCommand", command.getCommandType());
        assertEquals(userId, command.getUserId());
        assertEquals("TermService", command.getServiceName());
        assertTrue(command.isSaveOnUndoRedoStack());
    }

    @Test
    @SneakyThrows
    void findTermsByProjectReturnsPageOfTerms() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/project/{id}", projectId))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        SpringPage<TermResponse> page = objectMapper.readValue(response,
            new TypeReference<SpringPage<TermResponse>>() {});

        assertEquals(1, page.getTotalElements());
        TermResponse expected = new TermResponse(entity.getId(), "term1", "#FF0000", ontologyId,
            entity.getCreated(), entity.getUpdated(), "", null);
        assertEquals(List.of(expected), page.getContent());
    }

    @Test
    @SneakyThrows
    void findTermsByOntologyReturnsPageOfTerms() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        String response = mockMvc.perform(get("/terms/ontology/{id}", ontologyId))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        SpringPage<TermResponse> page = objectMapper.readValue(response,
            new TypeReference<SpringPage<TermResponse>>() {});

        assertEquals(1, page.getTotalElements());
        TermResponse expected = new TermResponse(entity.getId(), "term1", "#FF0000", ontologyId,
            entity.getCreated(), entity.getUpdated(), "", null);
        assertEquals(List.of(expected), page.getContent());
    }

    @Test
    @SneakyThrows
    void createWhenUserHasNoWriteAccessReturnsEmpty() {
        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);

        mockMvc.perform(post("/terms")
                .param("userId", nonAdminUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTerm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertTrue(termRepository.findAll().isEmpty());
    }

    private TermEntity createAndSaveTermEntity(String name, String color) {
        Date now = new Date();
        return termRepository.saveAndFlush(
            new TermEntity(null, 0, ontologyId, name, color, now, now, "", null));
    }
}
