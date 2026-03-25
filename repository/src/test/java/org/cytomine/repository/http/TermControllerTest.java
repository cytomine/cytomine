package org.cytomine.repository.http;

import java.util.Date;
import java.util.Optional;

import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class TermControllerTest {

    @Autowired
    private TermController termController;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ontologyId;
    private Long projectId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId =
            jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);

        ontologyId =
            jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
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
    void findTermByIdWhenExistsReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        Optional<TermResponse> result = termController.findTermByID(entity.getId());

        assertTrue(result.isPresent());
        assertEquals("term1", result.get().name());
    }

    @Test
    void findTermByIdWhenNotExistsReturnsEmpty() {
        Optional<TermResponse> result = termController.findTermByID(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllReturnsPageOfTerms() {
        createAndSaveTermEntity("term1", "#FF0000");
        createAndSaveTermEntity("term2", "#00FF00");

        Page<TermResponse> result = termController.findAll(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void createSavesAndReturnsTerm() {
        CreateTerm createTerm = new CreateTerm("newTerm", "#00FF00", ontologyId, null);

        Optional<HttpCommandResponse<TermResponse>> result =
            termController.create(userId, createTerm);

        assertEquals("newTerm", result.get().data().name());
        assertEquals("#00FF00", result.get().data().color());
        assertEquals(ontologyId, result.get().data().ontologyId());
    }

    @Test
    void updateWhenExistsUpdatesAndReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("oldName", "#FF0000");
        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.of("#00FF00"));

        Optional<HttpCommandResponse<TermResponse>> result = termController.update(entity.getId()
            , 0, updateTerm);

        assertEquals("newName", result.get().data().name());
        assertEquals("#00FF00", result.get().data().color());
    }

    @Test
    void updateWhenNotExistsThrowsException() {
        UpdateTerm updateTerm = new UpdateTerm(Optional.of("newName"), Optional.empty());

        assertEquals(Optional.empty(), termController.update(999L, 0, updateTerm));
    }

    @Test
    void deleteWhenExistsDeletesAndReturnsTerm() {
        TermEntity entity = createAndSaveTermEntity("term1", "#FF0000");

        Optional<HttpCommandResponse<TermResponse>> result =
            termController.delete(entity.getId(), 0);

        assertTrue(result.isPresent());
        assertEquals("term1", result.get().data().name());
        assertTrue(termRepository.findById(entity.getId()).isEmpty());
    }

    @Test
    void findTermsByProjectReturnsPageOfTerms() {
        createAndSaveTermEntity("term1", "#FF0000");

        Page<TermResponse> result =
            termController.findTermsByProject(projectId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findTermsByOntologyReturnsPageOfTerms() {
        createAndSaveTermEntity("term1", "#FF0000");

        Page<TermResponse> result =
            termController.findTermsByOntology(ontologyId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    private TermEntity createAndSaveTermEntity(String name, String color) {
        Date now = new Date();
        return termRepository.saveAndFlush(
            new TermEntity(null, 0, ontologyId, name, color, now, now, "", null));
    }
}
