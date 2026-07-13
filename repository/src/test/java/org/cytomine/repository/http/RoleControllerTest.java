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
import be.cytomine.common.repository.http.RoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.role.payload.CreateRole;
import be.cytomine.common.repository.model.role.payload.UpdateRole;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class RoleControllerTest implements CRUDCommandTests<CreateRole, RoleResponse, UpdateRole> {

    String apiURL = RoleHttpContract.ROOT_PATH;
    CreateRole createPayload = new CreateRole("ROLE_" + UUID.randomUUID().toString().toUpperCase());
    UpdateRole updatePayload = new UpdateRole(Optional.of("ROLE_" + UUID.randomUUID().toString().toUpperCase()));

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;

    @Override
    public RoleResponse expectedUpdatedResponse(RoleResponse response, UpdateRole updatePayload,
        LocalDateTime updatedTime) {
        return new RoleResponse(
            response.id(),
            updatePayload.authority().orElse(response.authority()),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
