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
import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class UserRoleControllerTest implements CRUDCommandTests<CreateUserRole, UserRoleResponse, UpdateUserRole> {

    String apiURL = UserRoleHttpContract.ROOT_PATH;
    CreateUserRole createPayload;
    UpdateUserRole updatePayload = new UpdateUserRole(Optional.empty());

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;

    @Override
    public void beforeCreate(long userId) {
        Long targetUserId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO sec_user (id, version, username) VALUES (?, 0, ?)",
            targetUserId, UUID.randomUUID());

        Long roleId = jdbcTemplate.queryForObject(
            "SELECT id FROM sec_role WHERE authority = 'ROLE_ADMIN'", Long.class);

        createPayload = new CreateUserRole(targetUserId, roleId);
    }

    @Override
    public UserRoleResponse expectedUpdatedResponse(UserRoleResponse response, UpdateUserRole updatePayload,
        LocalDateTime updatedTime) {
        return new UserRoleResponse(
            response.id(),
            response.userId(),
            updatePayload.roleId().orElse(response.roleId()),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
