package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class ApplyCommandServiceTest {

    @Autowired
    private ApplyCommandService applyCommandService;

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
    void undoDeleteTermCommandRestoresTermWithNewId() {
        CreateTerm createTerm = new CreateTerm("termToDelete", "#FF0000", ontologyId, null);
        LocalDateTime now = LocalDateTime.now();
        HttpCommandResponse<TermResponse> createResponse =
            termCommandService.createTerm(userId, createTerm, now).orElseThrow();

        Long originalTermId = createResponse.data().id();
        Optional<TermEntity> entity = termRepository.findById(originalTermId);
        assertTrue(entity.isPresent());

        HttpCommandResponse<TermResponse> deleteResponse =
            termCommandService.deleteTerm(originalTermId, userId, now).orElseThrow();
        UUID deleteCommandId = deleteResponse.command();
        entity.get().setDeleted(now);
        assertEquals(termRepository.findById(originalTermId), entity);
        Optional<Long> undoResult = applyCommandService.undoCommand(userId, deleteCommandId);

        assertTrue(undoResult.isPresent());

        TermEntity restoredTerm = termRepository.findById(undoResult.get()).orElseThrow();
        assertEquals("termToDelete", restoredTerm.getName());
        assertEquals("#FF0000", restoredTerm.getColor());
        assertEquals(ontologyId, restoredTerm.getOntologyId());
    }

    @Test
    void undoCommandWithNonExistentCommandIdReturnsFalse() {
        Optional<Long> result = applyCommandService.undoCommand(userId, UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    void undoCommandByUserWithoutPermissionReturnsFalse() {
        CreateTerm createTerm = new CreateTerm("termToDelete", "#FF0000", ontologyId, null);
        LocalDateTime now = LocalDateTime.now();
        HttpCommandResponse<TermResponse> createResponse =
            termCommandService.createTerm(userId, createTerm, now).orElseThrow();

        Long termId = createResponse.data().id();
        HttpCommandResponse<TermResponse> deleteResponse =
            termCommandService.deleteTerm(termId, userId, now).orElseThrow();
        UUID deleteCommandId = deleteResponse.command();

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        Optional<Long> result = applyCommandService.undoCommand(nonAdminUserId, deleteCommandId);

        assertFalse(result.isPresent());
        assertEquals(termRepository.findById(termId).map(TermEntity::getDeleted), deleteResponse.data().deleted());
    }

    @Test
    void undoInsertTermCommandDeletesCreatedTerm() {
        CreateTerm createTerm = new CreateTerm("termToUndo", "#00FF00", ontologyId, null);
        LocalDateTime now = LocalDateTime.now();
        HttpCommandResponse<TermResponse> createResponse =
            termCommandService.createTerm(userId, createTerm, now).orElseThrow();

        Long termId = createResponse.data().id();
        UUID insertCommandId = createResponse.command();
        assertTrue(termRepository.findById(termId).isPresent());
        Optional<Long> undoResult = applyCommandService.undoCommand(userId, insertCommandId);

        assertFalse(undoResult.isPresent());
        assertTrue(termRepository.findById(termId).isEmpty());
    }

    @Test
    void undoUpdateTermCommandRestoresPreviousState() {
        CreateTerm createTerm = new CreateTerm("originalName", "#FF0000", ontologyId, null);
        LocalDateTime now = LocalDateTime.now();
        HttpCommandResponse<TermResponse> createResponse =
            termCommandService.createTerm(userId, createTerm, now).orElseThrow();

        Long termId = createResponse.data().id();

        UpdateTerm updateTerm = new UpdateTerm(Optional.of("updatedName"), Optional.of("#00FF00"));
        HttpCommandResponse<TermResponse> updateResponse =
            termCommandService.updateTerm(termId, userId, updateTerm, now)
                .orElseThrow();

        UUID updateCommandId = updateResponse.command();
        TermEntity updatedTerm = termRepository.findById(termId).orElseThrow();
        assertEquals("updatedName", updatedTerm.getName());
        assertEquals("#00FF00", updatedTerm.getColor());

        Optional<Long> undoResult = applyCommandService.undoCommand(userId, updateCommandId);

        assertTrue(undoResult.isPresent());
        assertEquals(termId, undoResult.get());

        TermEntity restoredTerm = termRepository.findById(termId).orElseThrow();
        assertEquals("originalName", restoredTerm.getName());
        assertEquals("#FF0000", restoredTerm.getColor());
    }
}
