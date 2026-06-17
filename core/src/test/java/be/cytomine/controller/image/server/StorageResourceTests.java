package be.cytomine.controller.image.server;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.server.Storage;

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
@WithMockUser(username = "admin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class StorageResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private BasicInstanceBuilder builder;

    @MockitoBean
    private StorageHttpContract storageHttpContract;

    private static StorageResponse toResponse(Storage storage) {
        return new StorageResponse(
            storage.getId(),
            storage.getUser().getId(),
            storage.getName(),
            LocalDateTime.now(),
            Optional.empty(),
            Optional.empty()
        );
    }

    @Test
    @Transactional
    public void shouldListReadableStorages() throws Exception {
        Storage storage = builder.givenAStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.getAll(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(toResponse(storage))));

        mockMvc.perform(get("/api/storage.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + storage.getName() + "')]").exists());
    }

    @Test
    @Transactional
    public void shouldReturnEmptyCollectionWhenNoStoragesAreReadable() throws Exception {
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.getAll(eq(userId), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/storage.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(0)));
    }

    @Test
    @Transactional
    public void shouldReturnStorageWithExpectedFieldsWhenItExists() throws Exception {
        Storage storage = builder.givenAStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.get(eq(storage.getId()), eq(userId))).thenReturn(Optional.of(toResponse(storage)));

        mockMvc.perform(get("/api/storage/{id}.json", storage.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(storage.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(storage.getUser().getId().intValue()))
            .andExpect(jsonPath("$.name").value(storage.getName()))
            .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenStorageDoesNotExist() throws Exception {
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.get(eq(0L), eq(userId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/storage/{id}.json", 0))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldCreateStorageAndReturnCommandResponse() throws Exception {
        Storage storage = basicInstanceBuilder.givenANotPersistedStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(storageHttpContract.create(eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(
                true,
                new StorageResponse(
                    1L,
                    userId,
                    storage.getName(),
                    LocalDateTime.now(),
                    Optional.empty(),
                    Optional.empty()
                ),
                commandId, Commands.CREATE_STORAGE
            )));

        mockMvc.perform(post("/api/storage.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_STORAGE))
            .andExpect(jsonPath("$.data.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void shouldUpdateStorageAndReturnCommandResponse() throws Exception {
        Storage storage = builder.givenAStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(storageHttpContract.update(eq(storage.getId()), eq(userId), any()))
            .thenReturn(Optional.of(new HttpCommandResponse(
                true,
                toResponse(storage),
                commandId,
                Commands.UPDATE_STORAGE
            )));

        mockMvc.perform(put("/api/storage/{id}.json", storage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.UPDATE_STORAGE))
            .andExpect(jsonPath("$.data.id").value(storage.getId().intValue()))
            .andExpect(jsonPath("$.data.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenUpdatingNonExistentStorage() throws Exception {
        Storage storage = basicInstanceBuilder.givenANotPersistedStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.update(eq(0L), eq(userId), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/storage/{id}.json", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldDeleteStorageAndReturnCommandResponse() throws Exception {
        Storage storage = builder.givenAStorage(builder.givenDefaultAdmin());
        Long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(storageHttpContract.delete(eq(storage.getId()), eq(userId)))
            .thenReturn(Optional.of(new HttpCommandResponse(
                true,
                toResponse(storage),
                commandId,
                Commands.DELETE_STORAGE
            )));

        mockMvc.perform(delete("/api/storage/{id}.json", storage.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_STORAGE))
            .andExpect(jsonPath("$.data.id").value(storage.getId().intValue()))
            .andExpect(jsonPath("$.data.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void shouldReturnNotFoundWhenDeletingNonExistentStorage() throws Exception {
        Long userId = builder.givenDefaultAdmin().getId();
        when(storageHttpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/storage/{id}.json", 0)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
