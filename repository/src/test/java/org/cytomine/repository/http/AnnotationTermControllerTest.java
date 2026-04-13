package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.AnnotationTermRepository;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
import org.cytomine.repository.persistence.entity.AnnotationTermEntity;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.annotationterm.payload.CreateAnnotationTerm;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.response.AnnotationTermResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class AnnotationTermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnnotationTermRepository annotationTermRepository;

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
    }

    @Test
    @SneakyThrows
    void findByIdWhenExistsReturnsAnnotationTerm() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);

        String response = mockMvc.perform(get("/annotation_terms/{id}", entity.getId())
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        AnnotationTermResponse result = objectMapper.readValue(response, AnnotationTermResponse.class);
        assertEquals(entity.getId(), result.id());
        assertEquals(userAnnotationId, result.userAnnotationId());
        assertEquals(termId, result.termId());
    }

    @Test
    @SneakyThrows
    void findByIdWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(get("/annotation_terms/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, AnnotationTermResponse.class));
    }

    @Test
    @SneakyThrows
    void findByIdWhenNoReadAccessReturnsEmpty() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);

        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/annotation_terms/{id}", entity.getId())
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, AnnotationTermResponse.class));
    }

    @Test
    @SneakyThrows
    void findByUserAnnotationReturnsAnnotationTerms() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);

        String response = mockMvc.perform(get("/annotation_terms/user_annotation/{annotationId}", userAnnotationId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<AnnotationTermResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(1, result.size());
        assertEquals(entity.getId(), result.get(0).id());
    }

    @Test
    @SneakyThrows
    void findByProjectReturnsAnnotationTerms() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);

        String response = mockMvc.perform(get("/annotation_terms/project/{projectId}", projectId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<AnnotationTermResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(1, result.size());
        assertEquals(entity.getId(), result.get(0).id());
    }

    @Test
    @SneakyThrows
    void findByProjectWhenNoReadAccessReturnsEmptyList() {
        long userAnnotationId = saveUserAnnotation();
        saveAnnotationTerm(userAnnotationId);

        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/annotation_terms/project/{projectId}", projectId)
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<AnnotationTermResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(List.of(), result);
    }

    @Test
    @SneakyThrows
    void countByTermReturnsCount() {
        long userAnnotationId = saveUserAnnotation();
        saveAnnotationTerm(userAnnotationId);

        String response = mockMvc.perform(get("/annotation_terms/term/{termId}/count", termId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(1L, Long.parseLong(response));
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsAnnotationTerm() {
        long userAnnotationId = saveUserAnnotation();
        CreateAnnotationTerm payload = new CreateAnnotationTerm(userAnnotationId, termId, userId);

        String response = mockMvc.perform(post("/annotation_terms")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        AnnotationTermResponse data = (AnnotationTermResponse) result.data();

        assertEquals(userAnnotationId, data.userAnnotationId());
        assertEquals(termId, data.termId());
        assertEquals(CommandType.INSERT_ANNOTATION_TERM_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void createWhenNoReadAccessReturnsEmpty() {
        long userAnnotationId = saveUserAnnotation();
        Long nonMemberUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonmember')", nonMemberUserId);
        CreateAnnotationTerm payload = new CreateAnnotationTerm(userAnnotationId, termId, userId);

        String response = mockMvc.perform(post("/annotation_terms")
                .param("userId", nonMemberUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenNoReadAccessReturnsEmpty() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);
        Long nonMemberUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonmember')", nonMemberUserId);

        String response = mockMvc.perform(delete("/annotation_terms/{id}", entity.getId())
                .param("userId", nonMemberUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsDeletesAndReturnsAnnotationTerm() {
        long userAnnotationId = saveUserAnnotation();
        AnnotationTermEntity entity = saveAnnotationTerm(userAnnotationId);

        String response = mockMvc.perform(delete("/annotation_terms/{id}", entity.getId())
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        AnnotationTermResponse data = (AnnotationTermResponse) result.data();

        assertEquals(entity.getId(), data.id());
        assertEquals(CommandType.DELETE_ANNOTATION_TERM_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void deleteWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(delete("/annotation_terms/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    private void setUpAnnotationInfrastructure() {
        Long uploadedFileId = nextId();
        jdbcTemplate.update(
            "INSERT INTO uploaded_file (id, version, user_id, content_type, ext, filename, original_filename, size, status) VALUES (?, 0, ?, 'image/png', 'png', 'test.png', 'test.png', 0, 0)",
            uploadedFileId, userId);

        Long abstractImageId = nextId();
        jdbcTemplate.update("INSERT INTO abstract_image (id, version) VALUES (?, 0)", abstractImageId);

        Long mimeId = nextId();
        jdbcTemplate.update("INSERT INTO mime (id, version, extension, mime_type) VALUES (?, 0, 'png', 'image/png')",
            mimeId);

        Long abstractSliceId = nextId();
        jdbcTemplate.update(
            "INSERT INTO abstract_slice (id, version, channel, image_id, mime_id, time, uploaded_file_id, z_stack) VALUES (?, 0, 0, ?, ?, 0, ?, 0)",
            abstractSliceId, abstractImageId, mimeId, uploadedFileId);

        imageId = nextId();
        jdbcTemplate.update(
            "INSERT INTO image_instance (id, version, base_image_id, count_image_job_annotations, count_image_reviewed_annotations, project_id, user_id, class) VALUES (?, 0, ?, 0, 0, ?, ?, 'be.cytomine.domain.image.ImageInstance')",
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

    private AnnotationTermEntity saveAnnotationTerm(long userAnnotationId) {
        LocalDateTime now = LocalDateTime.now();
        AnnotationTermEntity entity = new AnnotationTermEntity();
        entity.setUserAnnotationId(userAnnotationId);
        entity.setTermId(termId);
        entity.setUserId(userId);
        entity.setCreated(now);
        entity.setUpdated(now);
        entity.setVersion(0);
        return annotationTermRepository.saveAndFlush(entity);
    }

    private Long nextId() {
        return jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
    }
}
