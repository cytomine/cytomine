package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.EntityManager;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.TermRelationRepository;
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
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class TermRelationCommandServiceTest {

    @Autowired
    private TermRelationCommandService termRelationCommandService;

    @Autowired
    private TermCommandService termCommandService;

    @Autowired
    private TermRelationRepository termRelationRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ontologyId;
    private Long userId;
    private Long relationId;

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

        relationId = jdbcTemplate.queryForObject("SELECT id FROM relation WHERE name = 'parent'", Long.class);
    }

    @Test
    void createTermRelationPersistsOntologyIds() {
        LocalDateTime t0 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        TermResponse term1Response =
            (TermResponse) termCommandService.createTerm(userId, new CreateTerm("term1", "#FF0000", ontologyId, null),
                t0).orElseThrow().data();
        TermResponse term2Response =
            (TermResponse) termCommandService.createTerm(userId, new CreateTerm("term2", "#00FF00", ontologyId, null),
                t0).orElseThrow().data();

        long term1Id = term1Response.id();
        long term2Id = term2Response.id();

        HttpCommandResponse response =
            termRelationCommandService.createTermRelation(userId, new CreateTermRelation(term1Id, term2Id, "parent"),
                t0).orElseThrow();

        TermRelationResponse termRelationResponse = (TermRelationResponse) response.data();
        long termRelationId = termRelationResponse.id();

        entityManager.flush();
        entityManager.clear();

        TermRelationEntity saved = termRelationRepository.findById(termRelationId).orElseThrow();

        assertNotNull(saved);
        assertEquals(term1Id, saved.getTerm1Id());
        assertEquals(term2Id, saved.getTerm2Id());
        assertEquals(relationId, saved.getRelationId());
    }
}
