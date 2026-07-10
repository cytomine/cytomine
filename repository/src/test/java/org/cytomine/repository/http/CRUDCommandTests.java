package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UndoCommandResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
public interface CRUDCommandTests<C, R extends ApplyCommandResponse, U> {
    MockMvc getMockMvc();

    ObjectMapper getObjectMapper();

    String getApiURL();

    C getCreatePayload();

    U getUpdatePayload();

    R expectedUpdatedResponse(R response, U updatePayload, LocalDateTime updatedTime);

    JdbcTemplate getJdbcTemplate();

    ApplyCommandResponseMapper getApplyCommandResponseMapper();

    default Set<? extends ApplyCommandResponse> createSubEntities(long userId, long currentId) {
        return Set.of();
    }

    default void beforeCreate(long userId) {
    }

    default long createUser() {
        Long userId = getJdbcTemplate().queryForObject("SELECT nextval('hibernate_sequence')", Long.class);

        getJdbcTemplate().update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, ?)", userId,
            UUID.randomUUID());
        getJdbcTemplate().update(
            "INSERT INTO sec_role (version, authority) SELECT 0, 'ROLE_ADMIN' "
                + "WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_ADMIN')");
        Long userRoleId = getJdbcTemplate().queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        getJdbcTemplate().update(
            "INSERT INTO sec_user_sec_role (id, version, sec_user_id, sec_role_id) SELECT ?, 0, ?, (SELECT id FROM "
                + "sec_role WHERE authority = 'ROLE_ADMIN')", userRoleId, userId);
        beforeCreate(userId);
        return userId;
    }

    @Test
    @SneakyThrows
    default void baseTest() {
        // Create Entity
        long userId = createUser();
        String stringUserId = String.valueOf(userId);
        String response = getMockMvc().perform(
                post(getApiURL()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getCreatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = getObjectMapper().readValue(response, HttpCommandResponse.class);

        // Add SubEntities
        createSubEntities(userId, result.data().id());

        // Get the Entity with Sub Entities
        String get = getMockMvc().perform(
                get(getApiURL() + "/" + result.data().id()).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ApplyCommandResponse getResponse = getObjectMapper().readValue(get, ApplyCommandResponse.class);
        R getResponseData = (R) getResponse;

        // Update Entity
        String update = getMockMvc().perform(
                put(getApiURL() + "/" + result.data().id()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getUpdatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse updateResult = getObjectMapper().readValue(update, HttpCommandResponse.class);
        R updateDataResult = (R) updateResult.data();

        assertEquals(expectedUpdatedResponse(getResponseData, getUpdatePayload(), updateDataResult.updated()
                .orElseThrow(
                    () -> new IllegalStateException("Newly created entity should not have `updated` empty."))),
            updateDataResult);

        // Delete Entity
        String delete = getMockMvc().perform(
                delete(getApiURL() + "/" + result.data().id()).param("userId", stringUserId)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        HttpCommandResponse deleteResult = getObjectMapper().readValue(delete, HttpCommandResponse.class);
        assertEquals(getApplyCommandResponseMapper().setDeleteTime(updateDataResult,
                Optional.of(deleteResult.data().deleted().orElseThrow(
                    () -> new IllegalStateException("Deleted entity should not have `deleted` empty.")))),
            deleteResult.data());
    }

    @Test
    @SneakyThrows
    default void createCommandTest() {
        // Create Entity
        long userId = createUser();
        String stringUserId = String.valueOf(userId);

        Optional<HttpCommandResponse> maybeFirstCreate = getObjectMapper().readValue(getMockMvc().perform(
                post(getApiURL()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getCreatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<>() {});

        HttpCommandResponse firstCreate =
            maybeFirstCreate.orElseThrow(() -> new IllegalStateException("First creation should not be empty."));

        // Add SubEntities
        Set<? extends ApplyCommandResponse> ignored = createSubEntities(userId, firstCreate.data().id());

        // Get the Entity with Sub Entities
        String get = getMockMvc().perform(get(getApiURL() + "/" + firstCreate.data().id()).param("userId", stringUserId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ApplyCommandResponse getResponse = getObjectMapper().readValue(get, ApplyCommandResponse.class);
        R getResponseData = (R) getResponse;

        String commandID = firstCreate.commandId().toString();
        long entityID = firstCreate.data().id();

        // Undo (Entity Creation)
        Optional<HttpCommandResponse> undoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(
                post(CommandController.ROOT_PATH + "/undo/" + commandID).param("userId", stringUserId)
                    .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
            .getContentAsString(), new TypeReference<>() {});

        LocalDateTime deletedTime =
            undoCommandResponse.orElseThrow(() -> new IllegalStateException("Response should not be empty.")).data()
                .deleted().orElseThrow(() -> new IllegalStateException("Deleted should not be empty."));
        assertEquals(Optional.of(new UndoCommandResponse(
                getApplyCommandResponseMapper().setDeleteTime(getResponseData, Optional.of(deletedTime)))),
            undoCommandResponse.map(HttpCommandResponse::data));

        String emptyResponseString = getMockMvc().perform(
                get(getApiURL() + "/" + entityID).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Optional<R> emptyResponse = getObjectMapper().readValue(emptyResponseString,
            getObjectMapper().constructType(Optional.of(firstCreate.data()).getClass()));

        assertEquals(emptyResponse, Optional.empty());

        // Undo (Undo (Entity Creation)) -> Recreate Entity
        Optional<HttpCommandResponse> redoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(
                post(CommandController.ROOT_PATH + "/undo/" + undoCommandResponse.get().commandId()).param("userId",
                    stringUserId).contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
            .getContentAsString(), new TypeReference<>() {});

        LocalDateTime updateTime = redoCommandResponse.stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Response should not be empty.")).data().updated()
            .orElseThrow(() -> new IllegalStateException("Updated should not be empty."));

        R firstCreateData = (R) firstCreate.data();
        assertEquals(Optional.of(new UndoCommandResponse(
                getApplyCommandResponseMapper().setUpdateTime(getResponseData, Optional.of(updateTime)))),
            redoCommandResponse.map(HttpCommandResponse::data));

        String redoGetResponseString = getMockMvc().perform(
                get(getApiURL() + "/" + entityID).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        R redoGetResponse = (R) getObjectMapper().readValue(redoGetResponseString, (firstCreateData).getClass());

        assertEquals(getApplyCommandResponseMapper().setUpdateTime(getResponse, Optional.of(
                redoGetResponse.updated().orElseThrow(
                    () -> new IllegalStateException("Newly re-created entity should not have `updated` empty.")))),
            getObjectMapper().readValue(redoGetResponseString, firstCreate.data().getClass()));
    }
}
