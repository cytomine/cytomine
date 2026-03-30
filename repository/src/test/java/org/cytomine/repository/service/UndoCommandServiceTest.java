package org.cytomine.repository.service;

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
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class UndoCommandServiceTest {

    @Autowired
    private UndoCommandService undoCommandService;

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
        HttpCommandResponse<TermResponse> createResponse = termCommandService.createTerm(userId, createTerm)
                                                               .orElseThrow();

        Long originalTermId = createResponse.data().id();
        assertTrue(termRepository.findById(originalTermId).isPresent());

        HttpCommandResponse<TermResponse> deleteResponse = termCommandService.deleteTerm(originalTermId, userId)
                                                               .orElseThrow();
        UUID deleteCommandId = deleteResponse.command();

        assertTrue(termRepository.findById(originalTermId).isEmpty());

        Optional<Long> undoResult = undoCommandService.undoCommand(userId, deleteCommandId);

        assertTrue(undoResult.isPresent());

        TermEntity restoredTerm = termRepository.findById(undoResult.get()).orElseThrow();
        assertEquals("termToDelete", restoredTerm.getName());
        assertEquals("#FF0000", restoredTerm.getColor());
        assertEquals(ontologyId, restoredTerm.getOntologyId());
    }

    @Test
    void undoCommandWithNonExistentCommandIdReturnsFalse() {
        Optional<Long> result = undoCommandService.undoCommand(userId, UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    void undoCommandByUserWithoutPermissionReturnsFalse() {
        CreateTerm createTerm = new CreateTerm("termToDelete", "#FF0000", ontologyId, null);
        HttpCommandResponse<TermResponse> createResponse = termCommandService.createTerm(userId, createTerm)
                                                               .orElseThrow();

        Long termId = createResponse.data().id();
        HttpCommandResponse<TermResponse> deleteResponse = termCommandService.deleteTerm(termId, userId)
                                                               .orElseThrow();
        UUID deleteCommandId = deleteResponse.command();

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        Optional<Long> result = undoCommandService.undoCommand(nonAdminUserId, deleteCommandId);

        assertFalse(result.isPresent());
        assertTrue(termRepository.findById(termId).isEmpty());
    }
}
