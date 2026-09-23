package org.cytomine.repository.http;

import java.util.Optional;
import java.util.UUID;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.CommandHttpContract;
import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.response.CommandV2Response;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;

import static org.cytomine.repository.http.SecurityMockMvcTestConfiguration.authenticatedAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import({PostGisTestConfiguration.class, SecurityMockMvcTestConfiguration.class})
public class CommandControllerTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String apiUrl = CommandHttpContract.ROOT_PATH;

    private long createUser() {
        String username = UUID.randomUUID().toString();
        long userId = jdbcTemplate.queryForObject(
            "INSERT INTO sec_user (version, username) VALUES (0, ?) RETURNING ID",
            Long.class,
            username
        );

        jdbcTemplate.update(
            "INSERT INTO sec_role (version, authority, created) SELECT 0, 'ROLE_ADMIN', NOW() "
                + "WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_ADMIN')"
        );

        jdbcTemplate.update(
            "INSERT INTO sec_user_sec_role (version, sec_user_id, created, sec_role_id) SELECT 0, ?, NOW(), (SELECT "
                + "id FROM sec_role WHERE authority = 'ROLE_ADMIN')", userId
        );

        return userId;
    }

    private String getUsername(long userId) {
        return jdbcTemplate.queryForObject("SELECT username FROM sec_user WHERE id = ?", String.class, userId);
    }

    @SneakyThrows
    private HttpCommandResponse createCommand(long userId) {
        String response = mockMvc.perform(post(OntologyHttpContract.ROOT_PATH)
                .with(authenticatedAs(getUsername(userId)))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateOntology(UUID.randomUUID().toString()))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readValue(response, HttpCommandResponse.class);
    }

    @Test
    @SneakyThrows
    void getCommandTest() {
        long userId = createUser();
        HttpCommandResponse command = createCommand(userId);

        mockMvc.perform(get(apiUrl + "/" + command.commandId()).with(authenticatedAs(getUsername(userId)))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(command.commandId().toString()))
            .andExpect(jsonPath("$.commandRequest.commandType").value(CommandType.INSERT_ONTOLOGY_COMMAND.toString()));
    }

    @Test
    @SneakyThrows
    void getUnknownCommandTest() {
        long userId = createUser();

        String response = mockMvc.perform(get(apiUrl + "/" + UUID.randomUUID())
                .with(authenticatedAs(getUsername(userId)))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
            .getContentAsString();

        assertEquals(
            Optional.empty(),
            objectMapper.readValue(response, new TypeReference<Optional<CommandV2Response<?>>>() {})
        );
    }

    @Test
    @SneakyThrows
    void getAllForUserTest() {
        long userId = createUser();
        HttpCommandResponse first = createCommand(userId);
        HttpCommandResponse second = createCommand(userId);

        mockMvc.perform(get(apiUrl + "/all").with(authenticatedAs(getUsername(userId)))
                .param("sort", "created,desc")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(second.commandId().toString()))
            .andExpect(jsonPath("$.content[1].id").value(first.commandId().toString()));
    }

    @Test
    @SneakyThrows
    void undoCommandTest() {
        long userId = createUser();
        HttpCommandResponse command = createCommand(userId);

        String response = mockMvc.perform(post(apiUrl + "/undo/" + command.commandId()).with(
                authenticatedAs(getUsername(userId))
            ).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Optional<HttpCommandResponse> undoResponse = objectMapper.readValue(response, new TypeReference<>() {});

        assertTrue(undoResponse.orElseThrow(() -> new IllegalStateException("Response should not be empty."))
            .data()
            .deleted()
            .isPresent());
    }
}
