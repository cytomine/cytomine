package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.UploadedFileHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class UploadedFileControllerTest
    implements CRUDCommandTests<CreateUploadedFile, UploadedFileResponse, UpdateUploadedFile> {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;
    String apiURL = UploadedFileHttpContract.ROOT_PATH;

    UpdateUploadedFile updatePayload = new UpdateUploadedFile(
        Optional.of(UUID.randomUUID() + ".tif"),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    CreateUploadedFile createPayload;

    @Override
    public void beforeCreate(long userId) {
        Long storageId = jdbcTemplate.queryForObject(
            "INSERT INTO storage (version, name, user_id, created) VALUES (0, ?, ?, NOW()) RETURNING id",
            Long.class, "test-storage", userId
        );
        createPayload = new CreateUploadedFile(
            userId,
            storageId,
            Optional.empty(),
            UUID.randomUUID() + ".tif",
            UUID.randomUUID() + ".tif",
            "tif",
            "image/tiff",
            1024L,
            0,
            Set.of()
        );
    }

    private long createUploadedFile(String stringUserId) throws Exception {
        String response = mockMvc.perform(post(apiURL).param("userId", stringUserId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, HttpCommandResponse.class).data().id();
    }

    private List<Long> getAllIds(String stringUserId, String... uploadedFileIds) throws Exception {
        var request = get(apiURL + "/all").param("userId", stringUserId);
        if (uploadedFileIds.length > 0) {
            request = request.param("uploadedFileIds", uploadedFileIds);
        }
        String response = mockMvc.perform(request).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<Long> ids = new ArrayList<>();
        for (JsonNode node : objectMapper.readTree(response).get("content")) {
            ids.add(node.get("id").asLong());
        }
        return ids;
    }

    @Test
    @SneakyThrows
    void getAllShouldReturnUploadedFileWhenNoIdFilter() {
        long userId = createUser();
        String stringUserId = String.valueOf(userId);
        long uploadedFileId = createUploadedFile(stringUserId);

        assertTrue(getAllIds(stringUserId).contains(uploadedFileId));
    }

    @Test
    @SneakyThrows
    void getAllShouldReturnOnlyUploadedFilesMatchingIdFilter() {
        long userId = createUser();
        String stringUserId = String.valueOf(userId);
        long uploadedFileId = createUploadedFile(stringUserId);

        assertEquals(List.of(uploadedFileId), getAllIds(stringUserId, String.valueOf(uploadedFileId)));
    }

    @Test
    @SneakyThrows
    void getAllShouldExcludeUploadedFilesNotMatchingIdFilter() {
        long userId = createUser();
        String stringUserId = String.valueOf(userId);
        long uploadedFileId = createUploadedFile(stringUserId);

        assertFalse(getAllIds(stringUserId, String.valueOf(uploadedFileId + 1_000_000)).contains(uploadedFileId));
    }

    @Override
    public UploadedFileResponse expectedUpdatedResponse(
        UploadedFileResponse response,
        UpdateUploadedFile updatePayload,
        LocalDateTime updatedTime
    ) {
        String newFilename = updatePayload.filename().orElse(response.filename());
        return new UploadedFileResponse(
            response.id(),
            response.user(),
            response.parent(),
            response.storage(),
            newFilename,
            response.originalFilename(),
            response.ext(),
            response.contentType(),
            response.size(),
            newFilename,
            response.status(),
            response.projects(),
            response.created(),
            Optional.of(updatedTime),
            response.deleted(),
            response.thumbnailUrl()
        );
    }
}
