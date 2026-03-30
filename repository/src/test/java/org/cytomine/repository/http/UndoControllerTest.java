package org.cytomine.repository.http;

import java.util.Optional;
import java.util.UUID;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.service.TermCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class UndoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TermCommandService termCommandService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ontologyId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);

        Long adminRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_role (id, version, authority) VALUES (?, 0, 'ROLE_ADMIN')", adminRoleId);
        Long userRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) VALUES (?, 0, ?, ?)",
            userRoleId, userId, adminRoleId);

        ontologyId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, 'test', ?)", ontologyId,
            userId);
    }

    @Test
    @SneakyThrows
    void undoInsertTermCommandDeletesCreatedTerm() {
        CreateTerm createTerm = new CreateTerm("termToUndo", "#00FF00", ontologyId, null);
        HttpCommandResponse<TermResponse> createResponse = termCommandService.createTerm(userId, createTerm)
                                                               .orElseThrow();

        Long termId = createResponse.data().id();
        UUID insertCommandId = createResponse.command();
        assertTrue(termRepository.findById(termId).isPresent());

        mockMvc.perform(post("/commands/undo/{commandId}", insertCommandId)
                            .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertTrue(termRepository.findById(termId).isEmpty());
    }

    @Test
    @SneakyThrows
    void undoUpdateTermCommandRestoresPreviousState() {
        CreateTerm createTerm = new CreateTerm("originalName", "#FF0000", ontologyId, null);
        HttpCommandResponse<TermResponse> createResponse = termCommandService.createTerm(userId, createTerm)
                                                               .orElseThrow();

        Long termId = createResponse.data().id();

        UpdateTerm updateTerm = new UpdateTerm(Optional.of("updatedName"), Optional.of("#00FF00"));
        HttpCommandResponse<TermResponse> updateResponse = termCommandService.updateTerm(termId, userId, updateTerm)
                                                               .orElseThrow();

        UUID updateCommandId = updateResponse.command();
        TermEntity updatedTerm = termRepository.findById(termId).orElseThrow();
        assertEquals("updatedName", updatedTerm.getName());
        assertEquals("#00FF00", updatedTerm.getColor());

        mockMvc.perform(post("/commands/undo/{commandId}", updateCommandId)
                            .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(termId.intValue()));

        TermEntity restoredTerm = termRepository.findById(termId).orElseThrow();
        assertEquals("originalName", restoredTerm.getName());
        assertEquals("#FF0000", restoredTerm.getColor());
    }

    @Test
    @SneakyThrows
    void undoWithNonExistentCommandIdReturnsEmpty() {
        mockMvc.perform(post("/commands/undo/{commandId}", UUID.randomUUID())
                            .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    void undoByUserWithoutPermissionReturnsEmpty() {
        CreateTerm createTerm = new CreateTerm("termToUndo", "#FF0000", ontologyId, null);
        HttpCommandResponse<TermResponse> createResponse = termCommandService.createTerm(userId, createTerm)
                                                               .orElseThrow();

        Long termId = createResponse.data().id();
        UUID insertCommandId = createResponse.command();

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        mockMvc.perform(post("/commands/undo/{commandId}", insertCommandId)
                            .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertTrue(termRepository.findById(termId).isPresent());
    }
}
