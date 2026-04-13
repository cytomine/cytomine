package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
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
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;
import be.cytomine.common.repository.model.userannotation.payload.CreateUserAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

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
class UserAnnotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAnnotationRepository userAnnotationRepository;

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
    }

    @Test
    @SneakyThrows
    void findByIdWhenExistsReturnsUserAnnotation() {
        long annotationId = saveUserAnnotation();

        String response = mockMvc.perform(get("/user_annotations/{id}", annotationId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        UserAnnotationResponse result = objectMapper.readValue(response, UserAnnotationResponse.class);
        assertEquals(annotationId, result.id());
        assertEquals(projectId, result.projectId());
        assertEquals(imageId, result.imageId());
    }

    @Test
    @SneakyThrows
    void findByIdWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(get("/user_annotations/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, UserAnnotationResponse.class));
    }

    @Test
    @SneakyThrows
    void findByIdWhenNoReadAccessReturnsEmpty() {
        long annotationId = saveUserAnnotation();
        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/user_annotations/{id}", annotationId)
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, UserAnnotationResponse.class));
    }

    @Test
    @SneakyThrows
    void findAllByUserAndImageReturnsUserAnnotations() {
        long annotationId = saveUserAnnotation();

        String response = mockMvc.perform(get("/user_annotations/user/{userAnnotationUserId}/image/{imageId}",
                userId, imageId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<UserAnnotationResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(1, result.size());
        assertEquals(annotationId, result.get(0).id());
    }

    @Test
    @SneakyThrows
    void countByProjectReturnsCount() {
        saveUserAnnotation();

        String response = mockMvc.perform(get("/user_annotations/count/project/{projectId}", projectId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(1L, Long.parseLong(response));
    }

    @Test
    @SneakyThrows
    void countByProjectWhenNoReadAccessReturnsZero() {
        saveUserAnnotation();
        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/user_annotations/count/project/{projectId}", projectId)
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(0L, Long.parseLong(response));
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsUserAnnotation() {
        CreateUserAnnotation payload = new CreateUserAnnotation(userId, imageId, sliceId, projectId,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0);

        String response = mockMvc.perform(post("/user_annotations")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        UserAnnotationResponse data = (UserAnnotationResponse) result.data();

        assertEquals(projectId, data.projectId());
        assertEquals(imageId, data.imageId());
        assertEquals(CommandType.INSERT_USER_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void createWhenNoAccessReturnsEmpty() {
        Long nonAdminUserId = nextId();
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        CreateUserAnnotation payload = new CreateUserAnnotation(userId, imageId, sliceId, projectId,
            "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", 0.0);

        String response = mockMvc.perform(post("/user_annotations")
                .param("userId", nonAdminUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void updateWhenExistsUpdatesAndReturnsUserAnnotation() {
        long annotationId = saveUserAnnotation();
        UpdateUserAnnotation payload = new UpdateUserAnnotation(
            Optional.of("POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))"), Optional.empty());

        String response = mockMvc.perform(put("/user_annotations/{id}", annotationId)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        UserAnnotationResponse data = (UserAnnotationResponse) result.data();

        assertEquals(annotationId, data.id());
        assertEquals("POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))", data.wktLocation());
        assertEquals(CommandType.UPDATE_USER_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void updateWhenNotExistsReturnsEmpty() {
        UpdateUserAnnotation payload = new UpdateUserAnnotation(Optional.empty(), Optional.empty());

        String response = mockMvc.perform(put("/user_annotations/{id}", 999L)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsDeletesAndReturnsUserAnnotation() {
        long annotationId = saveUserAnnotation();

        String response = mockMvc.perform(delete("/user_annotations/{id}", annotationId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        UserAnnotationResponse data = (UserAnnotationResponse) result.data();

        assertEquals(annotationId, data.id());
        assertTrue(data.deleted().isPresent());
        assertEquals(CommandType.DELETE_USER_ANNOTATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void deleteWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(delete("/user_annotations/{id}", 999L)
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

    private Long nextId() {
        return jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
    }
}
