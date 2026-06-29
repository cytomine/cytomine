package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import be.cytomine.common.repository.model.HasLocaleDateTimeCUD;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

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
public interface CRUDCommandTests<C, R extends HasLocaleDateTimeCUD, U> {
    MockMvc getMockMvc();

    ObjectMapper getObjectMapper();

    String getApiURL();

    C getCreatePayload();

    U getUpdatePayload();

    default Set<ApplyCommandResponse> expectDeletedSubEntities(LocalDateTime deletionTime) {
        return Set.of();
    }

    R expectedUpdatedResponse(R response, U updatePayload, LocalDateTime updatedTime);

    R expectedDeletedResponse(R response, LocalDateTime deletedTime);

    R expectChangedUpdatedTime(R response, LocalDateTime updatedTime);

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
            "INSERT INTO sec_role (id, version, authority) SELECT nextval('hibernate_sequence'), 0, 'ROLE_ADMIN' "
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
        long userId = createUser();
        String stringUserId = String.valueOf(userId);
        String response = getMockMvc().perform(
                post(getApiURL()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getCreatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse result = getObjectMapper().readValue(response, HttpCommandResponse.class);
        createSubEntities(userId, result.data().id());

        String get = getMockMvc().perform(
                get(getApiURL() + "/" + result.data().id()).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ApplyCommandResponse getResponse = getObjectMapper().readValue(get, ApplyCommandResponse.class);
        R getResponseData = (R) getResponse;

        String update = getMockMvc().perform(
                put(getApiURL() + "/" + result.data().id()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getUpdatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        HttpCommandResponse updateResult = getObjectMapper().readValue(update, HttpCommandResponse.class);
        R updateDataResult = (R) updateResult.data();

        assertEquals(expectedUpdatedResponse(getResponseData, getUpdatePayload(), updateDataResult.updated()
                .orElseThrow(
                    () -> new IllegalStateException("Newly created entity should " + "not have `updated` empty."))),
            updateDataResult);

        String delete = getMockMvc().perform(
                delete(getApiURL() + "/" + result.data().id()).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        HttpCommandResponse deleteResult = getObjectMapper().readValue(delete, HttpCommandResponse.class);
        assertEquals(expectedDeletedResponse(updateDataResult, deleteResult.data().deleted().orElseThrow()),
            deleteResult.data());
    }

    @Test
    @SneakyThrows
    default void createCommandTest() {
        long userId = createUser();
        String stringUserId = String.valueOf(userId);

        Optional<HttpCommandResponse> maybeFirstCreate = getObjectMapper().readValue(getMockMvc().perform(
                post(getApiURL()).param("userId", stringUserId).contentType(APPLICATION_JSON)
                    .content(getObjectMapper().writeValueAsString(getCreatePayload()))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<>() {});

        HttpCommandResponse firstCreate =
            maybeFirstCreate.orElseThrow(() -> new IllegalStateException("First creation should not be empty."));

        Set<? extends ApplyCommandResponse> subEntities = createSubEntities(userId, firstCreate.data().id());

        String get = getMockMvc().perform(get(getApiURL() + "/" + firstCreate.data().id()).param("userId", stringUserId)
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ApplyCommandResponse getResponse = getObjectMapper().readValue(get, ApplyCommandResponse.class);
        R getResponseData = (R) getResponse;

        String commandID = firstCreate.commandId().toString();
        long entityID = firstCreate.data().id();
        Set<HttpCommandResponse> undoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(
                post(CommandController.ROOT_PATH + "/undo/" + commandID).param("userId", stringUserId)
                    .contentType(APPLICATION_JSON).content(getObjectMapper().writeValueAsString(getCreatePayload())))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<>() {});

        LocalDateTime deletedTime = undoCommandResponse.stream().findFirst().get().data().deleted()
            .orElseThrow(() -> new IllegalStateException("Deleted should not be empty."));
        assertEquals(Stream.concat(Stream.of(expectedDeletedResponse(getResponseData, deletedTime)),
                    subEntities.stream().map(se -> getApplyCommandResponseMapper().setDeleteTime(se,
                        Optional.of(deletedTime))))
                .collect(Collectors.toSet()),
            undoCommandResponse.stream().map(HttpCommandResponse::data).collect(Collectors.toSet()));

        String emptyResponseString = getMockMvc().perform(
                get(getApiURL() + "/" + entityID).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Optional<R> emptyResponse = getObjectMapper().readValue(emptyResponseString,
            getObjectMapper().constructType(Optional.of(firstCreate.data()).getClass()));

        assertEquals(emptyResponse, Optional.empty());

        Set<HttpCommandResponse> redoCommandResponse = getObjectMapper().readValue(getMockMvc().perform(
                post(CommandController.ROOT_PATH + "/redo/" + commandID).param("userId", stringUserId)
                    .contentType(APPLICATION_JSON).content(getObjectMapper().writeValueAsString(getCreatePayload())))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<>() {});



        String redoGetResponseString = getMockMvc().perform(
                get(getApiURL() + "/" + entityID).param("userId", stringUserId).contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        R redoGetResponse = (R) getObjectMapper().readValue(redoGetResponseString, ((R) firstCreate.data()).getClass());

        assertEquals(expectChangedUpdatedTime((R) firstCreate.data(), redoGetResponse.updated().orElseThrow(
                () -> new IllegalStateException("Newly created entity should not have `updated` " + "empty."))),
            getObjectMapper().readValue(redoGetResponseString, firstCreate.data().getClass()));
    }
}
