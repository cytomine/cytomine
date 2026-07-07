package org.cytomine.repository.http;

import java.time.LocalDateTime;
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
import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class UserControllerTest implements CRUDCommandTests<CreateUser, UserResponse, UpdateUser> {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    String apiURL = UserHttpContract.ROOT_PATH;
    CreateUser createPayload =
        new CreateUser(UUID.randomUUID().toString(), Optional.empty(), Optional.empty(), UUID.randomUUID().toString(),
            Optional.empty());
    UpdateUser updatePayload =
        new UpdateUser(Optional.of(UUID.randomUUID().toString()), Optional.of(UUID.randomUUID().toString()),
            Optional.of(UUID.randomUUID().toString()), Optional.of(UUID.randomUUID().toString()),
            Optional.of(UUID.randomUUID().toString()));
    @Autowired
    private ApplyCommandResponseMapper applyCommandResponseMapper;

    @Override
    public UserResponse expectedUpdatedResponse(UserResponse response, UpdateUser updatePayload,
        LocalDateTime updatedTime) {
        return new UserResponse(response.id(), updatePayload.username().orElse(response.username()),
            updatePayload.email().orElse(response.email()), updatePayload.lastname().or(response::lastname),
            updatePayload.firstname().or(response::firstname), updatePayload.language().or(response::language),
            Optional.of(updatedTime), response.deleted(), response.created());
    }
}
