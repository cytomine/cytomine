package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class TermLifecycleTest {

    @Autowired
    private TermCommandService termCommandService;

    @Autowired
    private TermRelationCommandService termRelationCommandService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermRelationRepository termRelationRepository;

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
    void createTwoTermsMakeOneChildThenEditBothThenDeleteParent() {
        LocalDateTime t0 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime t1 = t0.plusSeconds(1);
        LocalDateTime t2 = t0.plusSeconds(2);
        final LocalDateTime t3 = t0.plusSeconds(3);

        // Step 1: create parent and child terms
        TermResponse parentResponse = (TermResponse) termCommandService
            .createTerm(userId, new CreateTerm("parent", "#FF0000", ontologyId, null), t0)
            .orElseThrow().data();
        long parentId = parentResponse.id();

        TermResponse childResponse = (TermResponse) termCommandService
            .createTerm(userId, new CreateTerm("child", "#00FF00", ontologyId, null), t0)
            .orElseThrow().data();
        long childId = childResponse.id();

        entityManager.flush();
        entityManager.clear();

        assertEquals("parent", termRepository.findById(parentId).orElseThrow().getName());
        assertEquals("child", termRepository.findById(childId).orElseThrow().getName());

        // Step 2: make child a child of parent via a term relation (term1=parent, term2=child)
        HttpCommandResponse relationCreationResponse = termRelationCommandService
            .createTermRelation(userId, new CreateTermRelation(parentId, childId, "parent"), t1)
            .orElseThrow();
        TermRelationResponse termRelationResponse = (TermRelationResponse) relationCreationResponse.data();
        long termRelationId = termRelationResponse.id();

        entityManager.flush();
        entityManager.clear();

        TermRelationEntity savedRelation = termRelationRepository.findById(termRelationId).orElseThrow();
        assertNotNull(savedRelation);
        assertEquals(parentId, savedRelation.getTerm1Id());
        assertEquals(childId, savedRelation.getTerm2Id());

        // Step 3: edit both terms
        HttpCommandResponse updateParentResponse = termCommandService
            .updateTerm(parentId, userId, new UpdateTerm(Optional.of("parent-updated"), Optional.of("#FF00FF")), t2)
            .orElseThrow();
        TermResponse updatedParent = (TermResponse) updateParentResponse.data();
        assertEquals("parent-updated", updatedParent.name());
        assertEquals("#FF00FF", updatedParent.color());

        HttpCommandResponse updateChildResponse = termCommandService
            .updateTerm(childId, userId, new UpdateTerm(Optional.of("child-updated"), Optional.of("#0000FF")), t2)
            .orElseThrow();
        TermResponse updatedChild = (TermResponse) updateChildResponse.data();
        assertEquals("child-updated", updatedChild.name());
        assertEquals("#0000FF", updatedChild.color());

        entityManager.flush();
        entityManager.clear();

        TermEntity savedParent = termRepository.findById(parentId).orElseThrow();
        assertEquals("parent-updated", savedParent.getName());
        assertEquals("#FF00FF", savedParent.getColor());

        TermEntity savedChild = termRepository.findById(childId).orElseThrow();
        assertEquals("child-updated", savedChild.getName());
        assertEquals("#0000FF", savedChild.getColor());

        // Step 4: delete the parent
        HttpCommandResponse deleteResponse = termCommandService.deleteTerm(parentId, userId, t3).orElseThrow();
        TermResponse deletedParent = (TermResponse) deleteResponse.data();
        assertTrue(deletedParent.deleted().isPresent());
        assertEquals(t3, deletedParent.deleted().get());

        entityManager.flush();
        entityManager.clear();

        TermEntity deletedEntity = termRepository.findById(parentId).orElseThrow();
        assertEquals(t3, deletedEntity.getDeleted());
    }
}
