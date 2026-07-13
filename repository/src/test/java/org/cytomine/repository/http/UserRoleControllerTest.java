package org.cytomine.repository.http;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.persistence.entity.RoleEntity;
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
import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.Role;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class UserRoleControllerTest implements CRUDCommandTests<CreateUserRole, UserRoleResponse, UpdateUserRole> {

    String apiURL = UserRoleHttpContract.ROOT_PATH;
    CreateUserRole createPayload;
    UpdateUserRole updatePayload = new UpdateUserRole(Optional.empty());
    @Autowired
    RoleRepository roleRepository;
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
        RoleEntity roleGuest = roleRepository.save(
            new RoleEntity(null, 0, UUID.randomUUID().toString(), Timestamp.from(Instant.now()), null, null));
        createPayload = new CreateUserRole(userId, roleGuest.getId());
    }

    @Override
    public UserRoleResponse expectedUpdatedResponse(UserRoleResponse response, UpdateUserRole updatePayload,
        LocalDateTime updatedTime) {
        return new UserRoleResponse(response.id(), response.userId(), updatePayload.roleId().orElse(response.roleId()),
            response.created(), Optional.of(updatedTime), response.deleted());
    }

    @Test
    @SneakyThrows
    void defineTest() {
        long userId = createUser();
        long targetUserId = createUser();
        String stringResultGuest = mockMvc.perform(
                put(getApiURL() + format("/define/%s/role/%s", targetUserId, Role.ROLE_GUEST)).param("userId",
                    String.valueOf(userId)).contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();
        Set<UserRoleResponse> resultGuest = getObjectMapper().readValue(stringResultGuest, new TypeReference<>() {
        });
        assertEquals(4, resultGuest.size());
        String stringResultAdmin = mockMvc.perform(
                put(getApiURL() + format("/define/%s/role/%s", targetUserId, Role.ROLE_SUPER_ADMIN)).param("userId",
                    String.valueOf(userId)).contentType(APPLICATION_JSON)).andExpect(status().isOk()).andReturn()
            .getResponse().getContentAsString();
        Set<UserRoleResponse> resultAdmin = getObjectMapper().readValue(stringResultAdmin, new TypeReference<>() {
        });
        assertEquals(4, resultAdmin.size());
    }
}
