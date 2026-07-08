package be.cytomine.controller.security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.RoleHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.config.MongoTestConfiguration;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class SecRoleResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleHttpContract roleHttpContract;

    private static RoleResponse roleResponse(long id, String authority) {
        return new RoleResponse(id, authority, LocalDateTime.now(), Optional.empty(), Optional.empty());
    }

    @Test
    @Transactional
    public void listAllRoles() throws Exception {
        when(roleHttpContract.list(any())).thenReturn(
            new PageImpl<>(List.of(roleResponse(1L, "ROLE_GUEST"), roleResponse(2L, "ROLE_USER"))));

        mockMvc.perform(get("/api/role.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))));
    }

    @Test
    @Transactional
    public void shouldSuccessfullyGetRole() throws Exception {
        RoleResponse role = roleResponse(1L, "ROLE_GUEST");
        when(roleHttpContract.get(1L)).thenReturn(Optional.of(role));

        mockMvc.perform(get("/api/role/{id}.json", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.authority").value("ROLE_GUEST"));
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenRoleDoesNotExist() throws Exception {
        when(roleHttpContract.get(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/role/{id}.json", 0L))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldCreateRoleAndReturnCommandResponse() throws Exception {
        UUID commandId = UUID.randomUUID();
        RoleResponse role = roleResponse(1L, "ROLE_NEW");
        when(roleHttpContract.create(any(Long.class), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, role, commandId, Commands.CREATE_ROLE, Set.of())));

        mockMvc.perform(post("/api/role.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ROLE_NEW\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_ROLE))
            .andExpect(jsonPath("$.data.authority").value("ROLE_NEW"));
    }

    @Test
    @Transactional
    public void shouldUpdateRoleAndReturnCommandResponse() throws Exception {
        UUID commandId = UUID.randomUUID();
        RoleResponse role = roleResponse(1L, "ROLE_UPDATED");
        when(roleHttpContract.update(eq(1L), any(Long.class), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, role, commandId, Commands.UPDATE_ROLE, Set.of())));

        mockMvc.perform(put("/api/role/{id}.json", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ROLE_UPDATED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.command").value(Commands.UPDATE_ROLE))
            .andExpect(jsonPath("$.data.authority").value("ROLE_UPDATED"));
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenUpdatingNonExistentRole() throws Exception {
        when(roleHttpContract.update(eq(0L), any(Long.class), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/role/{id}.json", 0L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ROLE_X\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldDeleteRoleAndReturnCommandResponse() throws Exception {
        UUID commandId = UUID.randomUUID();
        RoleResponse role = roleResponse(1L, "ROLE_GUEST");
        when(roleHttpContract.delete(eq(1L), any(Long.class))).thenReturn(
            Optional.of(new HttpCommandResponse(true, role, commandId, Commands.DELETE_ROLE, Set.of())));

        mockMvc.perform(delete("/api/role/{id}.json", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.command").value(Commands.DELETE_ROLE))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenDeletingNonExistentRole() throws Exception {
        when(roleHttpContract.delete(eq(0L), any(Long.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/role/{id}.json", 0L))
            .andExpect(status().isNotFound());
    }
}
