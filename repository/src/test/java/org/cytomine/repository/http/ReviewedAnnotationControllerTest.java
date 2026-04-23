package org.cytomine.repository.http;

import java.util.Set;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.ReviewedAnnotationLinkRepository;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationLinkEntity;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private ReviewedAnnotationLinkRepository reviewedAnnotationLinkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private Long reviewedAnnotationTermsId;
    private Long termId1;
    private Long termId2;

    @BeforeEach
    void setUp() {
        userId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'admin')", userId);
        Long adminRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_role (id, version, authority) VALUES (?, 0, 'ROLE_ADMIN')", adminRoleId);
        Long userRoleId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) " + "VALUES (?, 0, ?, ?)",
            userRoleId, userId, adminRoleId);

        Long ontologyId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, 'ontology', ?)",
            ontologyId, userId);

        Long abstractImageId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO abstract_image (id, version) VALUES (?, 0)", abstractImageId);

        Long projectId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO project (id, version, are_images_downloadable, blind_mode, count_annotations, count_images,"
                + " count_job_annotations, count_reviewed_annotations, hide_admins_layers,"
                + " hide_users_layers, is_closed, mode, name) "
                + "VALUES (?, 0, false, false, 0, 0, 0, 0, false, false, false, 'CLASSIC', 'project')", projectId);

        Long imageInstanceId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO image_instance (id, version, base_image_id, count_image_job_annotations,"
                + " count_image_reviewed_annotations, project_id, user_id, class) "
                + "VALUES (?, 0, ?, 0, 0, ?, ?, 'be.cytomine.domain.image.ImageInstance')", imageInstanceId,
            abstractImageId, projectId, userId);

        termId1 = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO term (id, version, name, color, ontology_id) VALUES (?, 0, 'term1', '#FF0000', ?)", termId1,
            ontologyId);

        termId2 = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO term (id, version, name, color, ontology_id) VALUES (?, 0, 'term2', '#00FF00', ?)", termId2,
            ontologyId);

        reviewedAnnotationTermsId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO reviewed_annotation (id, version, count_comments, image_id, location, parent_class_name,"
                + " parent_ident, review_user_id, status, user_id, wkt_location) "
                + "VALUES (?, 0, 0, ?, ST_GeomFromText('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))'), 'be.cytomine.domain"
                + ".ontology.UserAnnotation', 0, ?, 0, ?, 'POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')",
            reviewedAnnotationTermsId, imageInstanceId, userId, userId);
    }

    @Test
    @SneakyThrows
    void replaceAllTermIdsWhenNoExistingLinksCreatesAndReturnsNewLinks() {
        String response = mockMvc.perform(
                put("/reviewed-annotations/terms/{id}", reviewedAnnotationTermsId).param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Set.of(termId1))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Set<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(Set.of(termId1), result);
        assertEquals(Set.of(termId1), termIdsForAnnotation(reviewedAnnotationTermsId));
    }

    @Test
    @SneakyThrows
    void replaceAllTermIdsWhenLinksDifferFromExistingReplacesWithNewLinks() {
        reviewedAnnotationLinkRepository.saveAndFlush(
            new ReviewedAnnotationLinkEntity(termId1, reviewedAnnotationTermsId));

        String response = mockMvc.perform(
                put("/reviewed-annotations/terms/{id}", reviewedAnnotationTermsId).param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Set.of(termId2))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Set<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(Set.of(termId2), result);
        assertEquals(Set.of(termId2), termIdsForAnnotation(reviewedAnnotationTermsId));
    }

    @Test
    @SneakyThrows
    void replaceAllTermIdsWhenLinksMatchExistingReturnsSameLinks() {
        reviewedAnnotationLinkRepository.saveAndFlush(
            new ReviewedAnnotationLinkEntity(termId1, reviewedAnnotationTermsId));

        String response = mockMvc.perform(
                put("/reviewed-annotations/terms/{id}", reviewedAnnotationTermsId).param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Set.of(termId1))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Set<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(Set.of(termId1), result);
        assertEquals(Set.of(termId1), termIdsForAnnotation(reviewedAnnotationTermsId));
    }

    @Test
    @SneakyThrows
    void replaceAllTermIdsWhenNewLinksEmptyDeletesAllAndReturnsEmpty() {
        reviewedAnnotationLinkRepository.saveAndFlush(
            new ReviewedAnnotationLinkEntity(termId1, reviewedAnnotationTermsId));

        String response = mockMvc.perform(
                put("/reviewed-annotations/terms/{id}", reviewedAnnotationTermsId).param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Set.of())))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Set<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertTrue(result.isEmpty());
        assertTrue(
            reviewedAnnotationLinkRepository.findAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId).isEmpty());
    }

    @Test
    @SneakyThrows
    void replaceAllTermIdsWhenNoWriteAccessToOntologyReturnsEmpty() {
        reviewedAnnotationLinkRepository.saveAndFlush(
            new ReviewedAnnotationLinkEntity(termId1, reviewedAnnotationTermsId));

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(
                put("/reviewed-annotations/terms/{id}", reviewedAnnotationTermsId).param("userId",
                        nonAdminUserId.toString()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Set.of(termId2)))).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();

        Set<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertTrue(result.isEmpty());
        assertEquals(Set.of(termId1), termIdsForAnnotation(reviewedAnnotationTermsId));
    }

    private Set<Long> termIdsForAnnotation(long annotationTermsId) {
        return reviewedAnnotationLinkRepository.findAllByReviewedAnnotationTermsId(annotationTermsId).stream()
            .map(ReviewedAnnotationLinkEntity::getTermId).collect(java.util.stream.Collectors.toSet());
    }
}
