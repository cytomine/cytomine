package be.cytomine.controller.image.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.server.Storage;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
public class StorageResourceTests {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restStorageControllerMockMvc;

    @Test
    @Transactional
    public void listUserStorages() throws Exception {
        Storage storage = builder.givenAStorage();
        Storage otherUserStorage = builder.givenAStorage(builder.givenAUser());
        restStorageControllerMockMvc.perform(get("/api/storage.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + storage.getName() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.name=='" + otherUserStorage.getName() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listAllStorages() throws Exception {
        Storage storage = builder.givenAStorage();
        Storage otherUserStorage = builder.givenAStorage(builder.givenAUser());
        restStorageControllerMockMvc.perform(get("/api/storage.json").param("all", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + storage.getName() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.name=='" + otherUserStorage.getName() + "')]").exists());
    }

    @Test
    @Transactional
    public void shouldReturnStorageWithAllExpectedFields() throws Exception {
        Storage storage = builder.givenAStorage();

        restStorageControllerMockMvc.perform(get("/api/storage/{id}.json", storage.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(storage.getId().intValue()))
            .andExpect(jsonPath("$.class").value("be.cytomine.domain.image.server.Storage"))
            .andExpect(jsonPath("$.created").exists())
            .andExpect(jsonPath("$.name").value(storage.getName()))
            .andExpect(jsonPath("$.user").value(storage.getUser().getId().intValue()))
            .andExpect(jsonPath("$.basePath").doesNotExist()); //since multidim
    }

    @Test
    @Transactional
    public void addValidStorage() throws Exception {
        Storage storage =
            basicInstanceBuilder.givenANotPersistedStorage(builder.givenSuperAdmin());
        restStorageControllerMockMvc.perform(post("/api/storage.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.storageID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddStorageCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.storage.id").exists())
            .andExpect(jsonPath("$.storage.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void addStorageRefusedIfNameNotSet() throws Exception {
        Storage storage =
            basicInstanceBuilder.givenANotPersistedStorage(builder.givenSuperAdmin());
        storage.setName(null);
        restStorageControllerMockMvc.perform(post("/api/storage.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Transactional
    public void editValidStorage() throws Exception {
        Storage storage = builder.givenAStorage();
        restStorageControllerMockMvc.perform(put("/api/storage/{id}.json", storage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.storageID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditStorageCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.storage.id").exists())
            .andExpect(jsonPath("$.storage.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void deleteStorage() throws Exception {
        Storage storage = builder.givenAStorage();
        restStorageControllerMockMvc.perform(delete("/api/storage/{id}.json", storage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(storage.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.storageID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteStorageCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.storage.id").exists())
            .andExpect(jsonPath("$.storage.name").value(storage.getName()));
    }

    @Test
    @Transactional
    public void failWhenDeleteStorageNotExists() throws Exception {
        restStorageControllerMockMvc.perform(delete("/api/storage/{id}.json", 0)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").exists());
    }
}
