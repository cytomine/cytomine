package org.cytomine.repository.http;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class StorageControllerTest implements CRUDCommandTests<CreateStorage, StorageResponse, UpdateStorage> {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    String apiURL = StorageHttpContract.ROOT_PATH;
    CreateStorage createPayload = new CreateStorage(UUID.randomUUID().toString());
    UpdateStorage updatePayload = new UpdateStorage(Optional.of(UUID.randomUUID().toString()));
    @Autowired
    private ApplyCommandResponseMapper applyCommandResponseMapper;

    @Override
    public StorageResponse expectedUpdatedResponse(
        StorageResponse response,
        UpdateStorage updatePayload,
        Instant updatedTime
    ) {
        return new StorageResponse(
            response.id(),
            response.userId(),
            updatePayload.name().orElse(response.name()),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
