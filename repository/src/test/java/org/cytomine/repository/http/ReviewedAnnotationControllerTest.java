package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class ReviewedAnnotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewedAnnotationRepository reviewedAnnotationRepository;

    @Autowired
    private UserAnnotationRepository userAnnotationRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private CommandV2Repository commandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private Long projectId;
    private Long ontologyId;
    private Long imageId;
    private Long sliceId;
    private Long termId;
    private Long parentAnnotationId;

    @BeforeEach
    void setUp() {
        userId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);

        Long adminRoleId = nextId();
        jdbcTemplate.update("INSERT INTO sec_role (id, version, authority) VALUES (?, 0, 'ROLE_ADMIN')", adminRoleId);
        Long userRoleId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) VALUES (?, 0, ?, ?)",
            userRoleId, userId, adminRoleId);

        ontologyId = nextId();
        jdbcTemplate.update("INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, 'test', ?)", ontologyId,
            userId);

        projectId = nextId();
        jdbcTemplate.update("""
            INSERT INTO project (id, version, name, ontology_id, mode,
                are_images_downloadable, blind_mode, count_annotations, count_images,
                count_job_annotations, count_reviewed_annotations, hide_admins_layers,
                hide_users_layers, is_closed)
            VALUES (?, 0, 'test', ?, 'CLASSIC', false, false, 0, 0, 0, 0, false, false, false)
            """, projectId, ontologyId);

        setUpAnnotationInfrastructure();

        LocalDateTime now = LocalDateTime.now();
        TermEntity term = termRepository.saveAndFlush(
            new TermEntity(null, 0, ontologyId, "term1", "#FF0000", now, now, null, "", null));
        termId = term.getId();

        parentAnnotationId = saveUserAnnotation();
    }

    @Test
    @SneakyThrows
    void findByIdWhenExistsReturnsReviewedAnnotation() {
        long reviewedAnnotationId = saveReviewedAnnotation();

        String response = mockMvc.perform(get("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        ReviewedAnnotationResponse result = objectMapper.readValue(response, ReviewedAnnotationResponse.class);
        assertEquals(reviewedAnnotationId, result.id());
        assertEquals(projectId, result.projectId());
        assertEquals(imageId, result.imageId());
    }

    @Test
    @SneakyThrows
    void findByIdWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(get("/reviewed_annotations/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, ReviewedAnnotationResponse.class));
    }

    @Test
    @SneakyThrows
    void findByIdWhenNoReadAccessReturnsEmpty() {
        long reviewedAnnotationId = saveReviewedAnnotation();
        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, ReviewedAnnotationResponse.class));
    }

    @Test
    @SneakyThrows
    void countByTermReturnsCount() {
        long reviewedAnnotationId = saveReviewedAnnotation();
        jdbcTemplate.update(
            "INSERT INTO reviewed_annotation_term (reviewed_annotation_terms_id, term_id) VALUES (?, ?)",
            reviewedAnnotationId, termId);

        String response = mockMvc.perform(get("/reviewed_annotations/term/{termId}/count", termId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(1L, Long.parseLong(response));
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsReviewedAnnotation() {
        CreateReviewedAnnotation payload = new CreateReviewedAnnotation(userId, userId, imageId, sliceId, projectId,
            parentAnnotationId, "be.cytomine.domain.ontology.UserAnnotation", 0,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0, null);

        String response = mockMvc.perform(post("/reviewed_annotations")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        ReviewedAnnotationResponse data = (ReviewedAnnotationResponse) result.data();

        assertEquals(projectId, data.projectId());
        assertEquals(imageId, data.imageId());
        assertEquals(CommandType.INSERT_REVIEWED_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void createWhenNoAccessReturnsEmpty() {
        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        CreateReviewedAnnotation payload = new CreateReviewedAnnotation(userId, userId, imageId, sliceId, projectId,
            parentAnnotationId, "be.cytomine.domain.ontology.UserAnnotation", 0,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0, null);

        String response = mockMvc.perform(post("/reviewed_annotations")
                .param("userId", nonAdminUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void updateWhenExistsUpdatesAndReturnsReviewedAnnotation() {
        long reviewedAnnotationId = saveReviewedAnnotation();
        UpdateReviewedAnnotation payload = new UpdateReviewedAnnotation(
            Optional.of("POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))"), Optional.empty());

        String response = mockMvc.perform(put("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        ReviewedAnnotationResponse data = (ReviewedAnnotationResponse) result.data();

        assertEquals(reviewedAnnotationId, data.id());
        assertEquals("POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))", data.wktLocation());
        assertEquals(CommandType.UPDATE_REVIEWED_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void updateWhenNotReviewerReturnsEmpty() {
        long reviewedAnnotationId = saveReviewedAnnotation();
        Long nonReviewerUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonreviewer')",
            nonReviewerUserId);
        grantProjectReadAccess(nonReviewerUserId, "nonreviewer");
        UpdateReviewedAnnotation payload = new UpdateReviewedAnnotation(
            Optional.of("POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))"), Optional.empty());

        String response = mockMvc.perform(put("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", nonReviewerUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void updateWhenNotExistsReturnsEmpty() {
        UpdateReviewedAnnotation payload = new UpdateReviewedAnnotation(Optional.empty(), Optional.empty());

        String response = mockMvc.perform(put("/reviewed_annotations/{id}", 999L)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsDeletesAndReturnsReviewedAnnotation() {
        long reviewedAnnotationId = saveReviewedAnnotation();

        String response = mockMvc.perform(delete("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        ReviewedAnnotationResponse data = (ReviewedAnnotationResponse) result.data();

        assertEquals(reviewedAnnotationId, data.id());
        assertTrue(data.deleted().isPresent());
        assertEquals(CommandType.DELETE_REVIEWED_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void deleteWhenNotReviewerReturnsEmpty() {
        long reviewedAnnotationId = saveReviewedAnnotation();
        Long nonReviewerUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonreviewer2')",
            nonReviewerUserId);
        grantProjectReadAccess(nonReviewerUserId, "nonreviewer2");

        String response = mockMvc.perform(delete("/reviewed_annotations/{id}", reviewedAnnotationId)
                .param("userId", nonReviewerUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(delete("/reviewed_annotations/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    private void setUpAnnotationInfrastructure() {
        Long uploadedFileId = nextId();
        jdbcTemplate.update("""
                INSERT INTO uploaded_file
                (id, version, user_id, content_type, ext, filename, original_filename, size, status)
                VALUES (?, 0, ?, 'image/png', 'png', 'test.png', 'test.png', 0, 0)""",
            uploadedFileId, userId);

        Long abstractImageId = nextId();
        jdbcTemplate.update("INSERT INTO abstract_image (id, version) VALUES (?, 0)", abstractImageId);

        Long mimeId = nextId();
        jdbcTemplate.update("INSERT INTO mime (id, version, extension, mime_type) VALUES (?, 0, 'png', 'image/png')",
            mimeId);

        Long abstractSliceId = nextId();
        jdbcTemplate.update("""
                INSERT INTO abstract_slice
                (id, version, channel, image_id, mime_id, time, uploaded_file_id, z_stack)
                VALUES (?, 0, 0, ?, ?, 0, ?, 0)""",
            abstractSliceId, abstractImageId, mimeId, uploadedFileId);

        imageId = nextId();
        jdbcTemplate.update("""
                INSERT INTO image_instance
                (id, version, base_image_id, count_image_job_annotations,
                count_image_reviewed_annotations, project_id, user_id, class)
                VALUES (?, 0, ?, 0, 0, ?, ?, 'be.cytomine.domain.image.ImageInstance')""",
            imageId, abstractImageId, projectId, userId);

        sliceId = nextId();
        jdbcTemplate.update(
            "INSERT INTO slice_instance (id, version, base_slice_id, image_id, project_id) VALUES (?, 0, ?, ?, ?)",
            sliceId, abstractSliceId, imageId, projectId);
    }

    private long saveUserAnnotation() {
        LocalDateTime now = LocalDateTime.now();
        return userAnnotationRepository.insertWithGeometry(now, now, userId, imageId, sliceId, projectId,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0);
    }

    private long saveReviewedAnnotation() {
        LocalDateTime now = LocalDateTime.now();
        return reviewedAnnotationRepository.insertWithGeometry(now, now, userId, userId, imageId, sliceId, projectId,
            parentAnnotationId, "be.cytomine.domain.ontology.UserAnnotation", 0,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0);
    }

    private void grantProjectReadAccess(Long targetUserId, String username) {
        Long aclClassId = nextId();
        jdbcTemplate.update("INSERT INTO acl_class (id, class) VALUES (?, 'be.cytomine.domain.project.Project')",
            aclClassId);
        Long aclSidId = nextId();
        jdbcTemplate.update("INSERT INTO acl_sid (id, principal, sid) VALUES (?, true, ?)", aclSidId, username);
        Long aclOiId = nextId();
        jdbcTemplate.update("""
                INSERT INTO acl_object_identity
                (id, object_id_class, object_id_identity, entries_inheriting)
                VALUES (?, ?, ?, false)""",
            aclOiId, aclClassId, projectId);
        jdbcTemplate.update("""
                INSERT INTO acl_entry
                (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
                VALUES (?, ?, 0, ?, 1, true, false, false)""",
            nextId(), aclOiId, aclSidId);
    }

    private Long nextId() {
        return jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
    }
}
