package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.HasLocaleDateTimeCUD;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
public interface CRUDCommandTests<C, R extends HasLocaleDateTimeCUD, U> {
    MockMvc getMockMvc();

    ObjectMapper getObjectMapper();

    String getApiURL();

    C getCreatePayload();

    U getUpdatePayload();

    R expectedUpdatedResponse(R response, U updatePayload, LocalDateTime updatedTime);

    R expectedDeletedResponse(R response, LocalDateTime deletedTime);

    R expectChangedUpdatedTime(R response, LocalDateTime updatedTime);

    JdbcTemplate getJdbcTemplate();

    default void beforeCreate(long userId) {
    }

    default String createUser() {
        Long userId = getJdbcTemplate().queryForObject("SELECT nextval('hibernate_sequence')", Long.class);

        getJdbcTemplate().update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, ?)",
            userId,
            UUID.randomUUID());
        getJdbcTemplate().update(
            "INSERT INTO sec_role (id, version, authority) SELECT nextval('hibernate_sequence'), 0, 'ROLE_ADMIN' "
                + "WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_ADMIN')");
        Long userRoleId = getJdbcTemplate().queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        getJdbcTemplate().update(
            "INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) SELECT ?, 0, ?, (SELECT id FROM "
                + "sec_role WHERE authority = 'ROLE_ADMIN')",
            userRoleId,
            userId);
        beforeCreate(userId);
        return userId.toString();
    }

    @Test
    @SneakyThrows
    default void baseTest() {
        String userId = createUser();
        String response = getMockMvc().perform(post(getApiURL()).param("userId", userId)
                .contentType(APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(getCreatePayload())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        HttpCommandResponse result = getObjectMapper().readValue(response, HttpCommandResponse.class);

        R dataResult = (R) result.data();

        String get = getMockMvc().perform(get(getApiURL() + "/" + result.data().id()).param("userId", userId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals(dataResult, getObjectMapper().readValue(get, dataResult.getClass()));

        String update = getMockMvc().perform(put(getApiURL() + "/" + result.data().id()).param("userId", userId)
                .contentType(APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(getUpdatePayload())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        HttpCommandResponse updateResult = getObjectMapper().readValue(update, HttpCommandResponse.class);
        R updateDataResult = (R) updateResult.data();

        assertEquals(expectedUpdatedResponse(dataResult,
            getUpdatePayload(),
            updateDataResult.updated()
                .orElseThrow(() -> new IllegalStateException(
                    "Newly created entity should " + "not have `updated` empty."))), updateDataResult);

        String delete = getMockMvc().perform(delete(getApiURL() + "/" + result.data().id()).param("userId", userId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        HttpCommandResponse deleteResult = getObjectMapper().readValue(delete, HttpCommandResponse.class);
        assertEquals(expectedDeletedResponse(updateDataResult, deleteResult.data().deleted().orElseThrow()),
            deleteResult.data());
    }

    @Test
    @SneakyThrows
    default void createCommandTest() {
        String userId = createUser();
        Optional<HttpCommandResponse> firstCreate =
            getObjectMapper().readValue(getMockMvc().perform(post(getApiURL()).param("userId", userId)
                    .contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getCreatePayload())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), new TypeReference<>() {});
        String commandID = firstCreate.get().commandId().toString();
        long entityID = firstCreate.get().data().id();
        Optional<HttpCommandResponse> undoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(post(
                CommandController.ROOT_PATH + "/undo/" + commandID).param("userId", userId)
                .contentType(APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(getCreatePayload())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), new TypeReference<>() {});

        assertTrue(undoCommandResponse.isPresent());

        String emptyResponseString = getMockMvc().perform(get(getApiURL() + "/" + entityID).param("userId", userId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Optional<R> emptyResponse = getObjectMapper().readValue(emptyResponseString,
            getObjectMapper().constructType(Optional.of(firstCreate.get().data()).getClass()));

        assertEquals(emptyResponse, Optional.empty());

        Optional<HttpCommandResponse> redoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(post(
                CommandController.ROOT_PATH + "/redo/" + commandID).param("userId", userId)
                .contentType(APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(getCreatePayload())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), new TypeReference<>() {});

        assertTrue(redoCommandResponse.isPresent());

        String redoGetResponseString = getMockMvc().perform(get(getApiURL() + "/" + entityID).param("userId", userId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        R redoGetResponse =
            (R) getObjectMapper().readValue(redoGetResponseString, ((R) firstCreate.get().data()).getClass());

        assertEquals(expectChangedUpdatedTime((R) firstCreate.get().data(),
                redoGetResponse.updated()
                    .orElseThrow(() -> new IllegalStateException(
                        "Newly created entity should not have `updated` " + "empty."))),
            getObjectMapper().readValue(redoGetResponseString, firstCreate.get().data().getClass()));
    }
}
