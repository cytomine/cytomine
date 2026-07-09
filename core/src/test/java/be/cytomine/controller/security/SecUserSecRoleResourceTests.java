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
import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.config.MongoTestConfiguration;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
public class SecUserSecRoleResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRoleHttpContract userRoleHttpContract;

    private static UserRoleResponse userRoleResponse(long id, long userId, long roleId) {
        return new UserRoleResponse(id, userId, roleId, LocalDateTime.now(), Optional.empty(), Optional.empty());
    }

    @Test
    @Transactional
    public void listRoles() throws Exception {
        when(userRoleHttpContract.listByUserId(eq(1L), any())).thenReturn(
            new PageImpl<>(List.of(
                userRoleResponse(1L, 1L, 10L),
                userRoleResponse(2L, 1L, 11L)
            )));

        mockMvc.perform(get("/api/user/{user}/role.json", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))));
    }

    @Test
    @Transactional
    public void listHighestRoles() throws Exception {
        when(userRoleHttpContract.getHighestByUserId(1L)).thenReturn(
            Optional.of(userRoleResponse(1L, 1L, 10L)));

        mockMvc.perform(get("/api/user/{user}/role.json", 1L).param("highest", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1)));
    }

    @Test
    @Transactional
    public void getRoles() throws Exception {
        when(userRoleHttpContract.getByUserIdAndRoleId(1L, 10L)).thenReturn(
            Optional.of(userRoleResponse(1L, 1L, 10L)));

        mockMvc.perform(get("/api/user/{user}/role/{role}.json", 1L, 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @Transactional
    public void getRoleNotFound() throws Exception {
        when(userRoleHttpContract.getByUserIdAndRoleId(0L, 0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/{user}/role/{role}.json", 0L, 0L))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void addValidRole() throws Exception {
        UUID commandId = UUID.randomUUID();
        UserRoleResponse response = userRoleResponse(1L, 2L, 10L);
        when(userRoleHttpContract.create(anyLong(), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, response, commandId, Commands.CREATE_USER_ROLE, Set.of())));

        mockMvc.perform(post("/api/user/{user}/role.json", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_USER_ROLE));
    }

    @Test
    @Transactional
    public void deleteUserRole() throws Exception {
        UUID commandId = UUID.randomUUID();
        UserRoleResponse userRole = userRoleResponse(5L, 2L, 10L);
        when(userRoleHttpContract.getByUserIdAndRoleId(2L, 10L)).thenReturn(Optional.of(userRole));
        when(userRoleHttpContract.delete(eq(5L), anyLong())).thenReturn(
            Optional.of(new HttpCommandResponse(true, userRole, commandId, Commands.DELETE_USER_ROLE, Set.of())));

        mockMvc.perform(delete("/api/user/{user}/role/{role}.json", 2L, 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_USER_ROLE));
    }

    @Test
    @Transactional
    public void deleteUserRoleNotFound() throws Exception {
        when(userRoleHttpContract.getByUserIdAndRoleId(0L, 0L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/user/{user}/role/{role}.json", 0L, 0L))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void define() throws Exception {
        doNothing().when(userRoleHttpContract).define(eq(2L), eq(10L), anyLong());
        when(userRoleHttpContract.listByUserId(eq(2L), any())).thenReturn(
            new PageImpl<>(List.of(
                userRoleResponse(1L, 2L, 10L),
                userRoleResponse(2L, 2L, 11L)
            )));

        mockMvc.perform(put("/api/user/{user}/role/{role}/define.json", 2L, 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(2)));
    }
}
