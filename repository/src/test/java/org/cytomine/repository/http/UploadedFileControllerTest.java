package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.UploadedFileHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;

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
            Optional.empty()
        );
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
            response.statusText(),
            response.projects(),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }

    @Override
    public UploadedFileResponse expectedDeletedResponse(UploadedFileResponse response, LocalDateTime deletedTime) {
        return new UploadedFileResponse(
            response.id(),
            response.user(),
            response.parent(),
            response.storage(),
            response.filename(),
            response.originalFilename(),
            response.ext(),
            response.contentType(),
            response.size(),
            response.path(),
            response.status(),
            response.statusText(),
            response.projects(),
            response.created(),
            response.updated(),
            Optional.of(deletedTime)
        );
    }

    @Override
    public UploadedFileResponse expectChangedUpdatedTime(UploadedFileResponse response, LocalDateTime updatedTime) {
        return new UploadedFileResponse(
            response.id(),
            response.user(),
            response.parent(),
            response.storage(),
            response.filename(),
            response.originalFilename(),
            response.ext(),
            response.contentType(),
            response.size(),
            response.path(),
            response.status(),
            response.statusText(),
            response.projects(),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
