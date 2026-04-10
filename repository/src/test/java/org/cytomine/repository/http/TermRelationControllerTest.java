package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
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
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class TermRelationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermRelationRepository termRelationRepository;

    @Autowired
    private CommandV2Repository commandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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

        jdbcTemplate.update("INSERT INTO relation (version, name) VALUES (0, 'parent')");
        relationId = jdbcTemplate.queryForObject("SELECT id FROM relation WHERE name = 'parent'", Long.class);
    }

    @Test
    @SneakyThrows
    void findTermRelationByIdWhenExistsReturnsTermRelation() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        String response = mockMvc.perform(get("/term_relations/{id}", entity.getId())
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        TermRelationResponse result = objectMapper.readValue(response, TermRelationResponse.class);
        TermRelationResponse expected = new TermRelationResponse(entity.getId(), term1.getId(), term2.getId(),
            ontologyId, relationId, entity.getUpdated(), Optional.empty(), entity.getCreated(), null);
        assertEquals(expected, result);
    }

    @Test
    @SneakyThrows
    void findTermRelationByIdWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(get("/term_relations/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, TermRelationResponse.class));
    }

    @Test
    @SneakyThrows
    void findTermRelationByIdWhenNoReadAccessReturnsEmpty() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/term_relations/{id}", entity.getId())
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, TermRelationResponse.class));
    }

    @Test
    @SneakyThrows
    void findAllByOntologyIdReturnsTermRelations() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        String response = mockMvc.perform(get("/term_relations/ontology/{ontologyId}", ontologyId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<TermRelationResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        TermRelationResponse expected = new TermRelationResponse(entity.getId(), term1.getId(), term2.getId(),
            ontologyId, relationId, entity.getUpdated(), Optional.empty(), entity.getCreated(), null);
        assertEquals(List.of(expected), result);
    }

    @Test
    @SneakyThrows
    void findAllByOntologyIdWhenNoReadAccessReturnsEmptyList() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(get("/term_relations/ontology/{ontologyId}", ontologyId)
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<TermRelationResponse> result = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(List.of(), result);
    }

    @Test
    @SneakyThrows
    void createSavesAndReturnsTermRelation() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        CreateTermRelation createTermRelation = new CreateTermRelation(term1.getId(), term2.getId(), "parent");

        String response = mockMvc.perform(post("/term_relations")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTermRelation)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        TermRelationResponse data = (TermRelationResponse) result.data();

        assertEquals(term1.getId(), data.term1Id());
        assertEquals(term2.getId(), data.term2Id());
        assertEquals(ontologyId, data.ontologyId());
        assertEquals(Commands.CREATE_TERM_RELATION, result.command());
        assertEquals(CommandType.INSERT_TERM_RELATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void createWhenNoWriteAccessReturnsEmpty() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        CreateTermRelation createTermRelation = new CreateTermRelation(term1.getId(), term2.getId(), "parent");

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(post("/term_relations")
                .param("userId", nonAdminUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTermRelation)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void createWhenRelationAlreadyExistsReturnsEmpty() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        createAndSaveTermRelationEntity(term1.getId(), term2.getId());
        CreateTermRelation createTermRelation = new CreateTermRelation(term1.getId(), term2.getId(), "parent");

        String response = mockMvc.perform(post("/term_relations")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTermRelation)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void updateWhenExistsUpdatesAndReturnsTermRelation() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermEntity term3 = createAndSaveTermEntity("term3", "#0000FF");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(Optional.empty(),
            Optional.of(term3.getId()), Optional.empty());

        String response = mockMvc.perform(put("/term_relations/{id}", entity.getId())
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTermRelation)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        TermRelationResponse data = (TermRelationResponse) result.data();

        assertEquals(term1.getId(), data.term1Id());
        assertEquals(term3.getId(), data.term2Id());
        assertEquals(Commands.UPDATE_TERM_RELATION, result.command());
    }

    @Test
    @SneakyThrows
    void updateWhenNotExistsReturnsEmpty() {
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(Optional.empty(), Optional.empty(),
            Optional.empty());

        String response = mockMvc.perform(put("/term_relations/{id}", 999L)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTermRelation)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenExistsSoftDeletesAndReturnsTermRelation() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        String response = mockMvc.perform(delete("/term_relations/{id}", entity.getId())
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructType(HttpCommandResponse.class));
        TermRelationResponse data = (TermRelationResponse) result.data();

        assertEquals(term1.getId(), data.term1Id());
        assertEquals(term2.getId(), data.term2Id());
        assertEquals(Commands.DELETE_TERM_RELATION, result.command());
        assertEquals(CommandType.DELETE_TERM_RELATION_COMMAND,
            commandRepository.findById(result.commandId()).orElseThrow().getData().getCommandType());
    }

    @Test
    @SneakyThrows
    void deleteWhenNotExistsReturnsEmpty() {
        String response = mockMvc.perform(delete("/term_relations/{id}", 999L)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    @Test
    @SneakyThrows
    void deleteWhenNoWriteAccessReturnsEmpty() {
        TermEntity term1 = createAndSaveTermEntity("term1", "#FF0000");
        TermEntity term2 = createAndSaveTermEntity("term2", "#00FF00");
        TermRelationEntity entity = createAndSaveTermRelationEntity(term1.getId(), term2.getId());

        Long nonAdminUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, 'nonadmin')", nonAdminUserId);

        String response = mockMvc.perform(delete("/term_relations/{id}", entity.getId())
                .param("userId", nonAdminUserId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(response, HttpCommandResponse.class));
    }

    private TermEntity createAndSaveTermEntity(String name, String color) {
        LocalDateTime now = LocalDateTime.now();
        return termRepository.saveAndFlush(new TermEntity(null, 0, ontologyId, name, color, now, now, null, "", null));
    }

    private TermRelationEntity createAndSaveTermRelationEntity(long term1Id, long term2Id) {
        LocalDateTime now = LocalDateTime.now();
        TermRelationEntity entity = new TermRelationEntity();
        entity.setTerm1Id(term1Id);
        entity.setTerm2Id(term2Id);
        entity.setRelationId(relationId);
        entity.setCreated(now);
        entity.setUpdated(now);
        return termRelationRepository.saveAndFlush(entity);
    }
}
