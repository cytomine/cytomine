package be.cytomine.controller.meta;

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
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TagHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.meta.Tag;
import be.cytomine.utils.JsonObject;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
public class TagResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagHttpContract httpContract;

    @Test
    @Transactional
    public void listAllTags() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        when(httpContract.list(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(toResponse(tag))));

        mockMvc.perform(get("/api/tag.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + tag.getName() + "')]").exists());
    }

    @Test
    @Transactional
    public void shouldReturnTagWithAllExpectedFields() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        when(httpContract.read(eq(tag.getId()), eq(userId))).thenReturn(Optional.of(toResponse(tag)));

        mockMvc.perform(get("/api/tag/{id}.json", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(tag.getId().intValue()))
            .andExpect(jsonPath("$.name").value(tag.getName()))
            .andExpect(jsonPath("$.creatorName").value(builder.givenSuperAdmin().getUsername()))
            .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Transactional
    public void addValidTag() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.create(eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tag), commandId, Commands.CREATE_TAG, Set.of())));

        String createTagJson = JsonObject.of("name", tag.getName()).toJsonString();

        mockMvc.perform(post("/api/tag.json").contentType(APPLICATION_JSON).content(createTagJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.AddTagCommand"))
            .andExpect(jsonPath("$.data.id").value(tag.getId()))
            .andExpect(jsonPath("$.data.name").value(tag.getName()));
    }

    @Test
    @Transactional
    public void addTagWithNoWriteAccessReturnsEmpty() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        when(httpContract.create(eq(userId), any())).thenReturn(Optional.empty());

        String createTagJson = JsonObject.of("name", tag.getName()).toJsonString();

        mockMvc.perform(post("/api/tag.json").contentType(APPLICATION_JSON).content(createTagJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Transactional
    public void editValidTag() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.update(eq(tag.getId()), eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tag), commandId, Commands.UPDATE_TAG, Set.of())));

        String updateTagJson = JsonObject.of("name", tag.getName()).toJsonString();

        mockMvc.perform(put("/api/tag/{id}.json", tag.getId()).contentType(APPLICATION_JSON).content(updateTagJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.EditTagCommand"))
            .andExpect(jsonPath("$.data.id").value(tag.getId()))
            .andExpect(jsonPath("$.data.name").value(tag.getName()));
    }

    @Test
    @Transactional
    public void failWhenEditingTagDoesNotExists() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        when(httpContract.update(eq(tag.getId()), eq(userId), any())).thenReturn(Optional.empty());

        String updateTagJson = JsonObject.of("name", tag.getName()).toJsonString();

        mockMvc.perform(put("/api/tag/{id}.json", tag.getId()).contentType(APPLICATION_JSON).content(updateTagJson))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteTag() throws Exception {
        Tag tag = builder.givenATag();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.delete(eq(tag.getId()), eq(userId))).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tag), commandId, Commands.DELETE_TAG, Set.of())));

        mockMvc.perform(delete("/api/tag/{id}.json", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.DeleteTagCommand"))
            .andExpect(jsonPath("$.data.id").value(tag.getId()))
            .andExpect(jsonPath("$.data.name").value(tag.getName()));
    }

    @Test
    @Transactional
    public void failWhenDeleteTagNotExists() throws Exception {
        long userId = builder.givenSuperAdmin().getId();
        when(httpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/tag/{id}.json", 0))
            .andExpect(status().isNotFound());
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getUser().getUsername(),
            LocalDateTime.now(),
            Optional.empty(),
            Optional.empty()
        );
    }
}
