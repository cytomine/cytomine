package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
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
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class TermCommandServiceTest {

    @Autowired
    private TermCommandService termCommandService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private CommandV2Repository commandV2Repository;

    @Autowired
    private EntityManager entityManager;

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
    void createThenDeleteThenUndoDeleteThenRedoDelete() {
        LocalDateTime t0 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t1 = t0.plusSeconds(1);
        LocalDateTime t2 = t0.plusSeconds(2);


        HttpCommandResponse createResponse =
            termCommandService.createTerm(userId, new CreateTerm("term1", "#FF0000", ontologyId, null), t0)
                .orElseThrow();
        TermResponse dataResult = (TermResponse) createResponse.data();
        long termId = dataResult.id();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t0, Optional.empty(), null, Set.of()),
            createResponse.command()), createResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 0, ontologyId, "term1", "#FF0000", t0, t0, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse deleteResponse = termCommandService.deleteTerm(termId, userId, t1).orElseThrow();
        UUID deleteCommandId = deleteResponse.command();
        DeleteTermCommand deleteCmd =
            (DeleteTermCommand) commandV2Repository.findById(deleteCommandId).orElseThrow().getData();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t0, Optional.of(t1), null, Set.of()),
            deleteCommandId), deleteResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 1, ontologyId, "term1", "#FF0000", t0, t0, t1, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse undoResponse =
            termCommandService.undoDeleteTerm(deleteCommandId, deleteCmd, userId, t2).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t2, Optional.empty(), null, Set.of()),
            deleteCommandId), undoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 2, ontologyId, "term1", "#FF0000", t0, t2, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        LocalDateTime t3 = t0.plusSeconds(3);
        HttpCommandResponse redoResponse =
            termCommandService.redoDeleteTerm(deleteCommandId, deleteCmd, userId, t3).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t2, Optional.of(t3), null, Set.of()),
            deleteCommandId), redoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 3, ontologyId, "term1", "#FF0000", t0, t2, t3, null, Set.of()),
            termRepository.findById(termId).orElseThrow());
    }

    @Test
    void createThenUpdateThenUndoUpdateThenRedoUpdate() {
        LocalDateTime t0 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t1 = t0.plusSeconds(1);


        HttpCommandResponse createResponse =
            termCommandService.createTerm(userId, new CreateTerm("original", "#FF0000", ontologyId, null), t0)
                .orElseThrow();
        TermResponse dataResult = (TermResponse) createResponse.data();
        long termId = dataResult.id();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "original", "#FF0000", ontologyId, t0, t0, Optional.empty(), null, Set.of()),
            createResponse.command()), createResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 0, ontologyId, "original", "#FF0000", t0, t0, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse updateResponse = termCommandService.updateTerm(termId, userId,
            new UpdateTerm(Optional.of("updated"), Optional.of("#00FF00")), t1).orElseThrow();
        UUID updateCommandId = updateResponse.command();
        UpdateTermCommand updateCmd =
            (UpdateTermCommand) commandV2Repository.findById(updateCommandId).orElseThrow().getData();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "updated", "#00FF00", ontologyId, t0, t0, Optional.empty(), null, Set.of()),
            updateCommandId), updateResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 1, ontologyId, "updated", "#00FF00", t0, t0, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse undoResponse =
            termCommandService.undoUpdateTerm(updateCommandId, updateCmd, userId).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "original", "#FF0000", ontologyId, t0, t0, Optional.empty(), null, Set.of()),
            updateCommandId), undoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 2, ontologyId, "original", "#FF0000", t0, t0, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        LocalDateTime t2 = t0.plusSeconds(2);
        HttpCommandResponse redoResponse =
            termCommandService.redoUpdateTerm(updateCommandId, updateCmd, userId, t2).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "updated", "#00FF00", ontologyId, t0, t2, Optional.empty(), null, Set.of()),
            updateCommandId), redoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 3, ontologyId, "updated", "#00FF00", t0, t2, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());
    }

    @Test
    void createThenUndoCreateThenRedoCreate() {
        LocalDateTime t0 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t1 = t0.plusSeconds(1);
        LocalDateTime t2 = t0.plusSeconds(2);

        HttpCommandResponse createResponse =
            termCommandService.createTerm(userId, new CreateTerm("term1", "#FF0000", ontologyId, null), t0)
                .orElseThrow();
        TermResponse dataResult = (TermResponse) createResponse.data();
        long termId = dataResult.id();
        UUID createCommandId = createResponse.command();
        CreateTermCommand createCmd =
            (CreateTermCommand) commandV2Repository.findById(createCommandId).orElseThrow().getData();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t0, Optional.empty(), null, Set.of()),
            createCommandId), createResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 0, ontologyId, "term1", "#FF0000", t0, t0, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse undoResponse =
            termCommandService.undoCreateTerm(createCommandId, createCmd, userId, t1).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t0, Optional.of(t1), null, Set.of()),
            createCommandId), undoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 1, ontologyId, "term1", "#FF0000", t0, t0, t1, null, Set.of()),
            termRepository.findById(termId).orElseThrow());

        HttpCommandResponse redoResponse =
            termCommandService.redoCreateTerm(createCommandId, createCmd, userId, t2).orElseThrow();

        assertEquals(new HttpCommandResponse(true,
            new TermResponse(termId, "term1", "#FF0000", ontologyId, t0, t2, Optional.empty(), null, Set.of()),
            createCommandId), redoResponse);
        entityManager.flush();
        entityManager.clear();
        assertEquals(new TermEntity(termId, 2, ontologyId, "term1", "#FF0000", t0, t2, null, null, Set.of()),
            termRepository.findById(termId).orElseThrow());
    }
}
