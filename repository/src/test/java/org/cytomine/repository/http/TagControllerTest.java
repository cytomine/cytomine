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
import be.cytomine.common.repository.http.TagHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.common.repository.model.tag.payload.CreateTag;
import be.cytomine.common.repository.model.tag.payload.UpdateTag;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class TagControllerTest implements CRUDCommandTests<CreateTag, TagResponse, UpdateTag> {
    String apiURL = TagHttpContract.ROOT_PATH;
    CreateTag createPayload = new CreateTag(UUID.randomUUID().toString());
    UpdateTag updatePayload = new UpdateTag(Optional.empty());

    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public TagResponse expectedUpdatedResponse(
        TagResponse response,
        UpdateTag updatePayload,
        LocalDateTime updatedTime
    ) {
        return new TagResponse(
            response.id(),
            updatePayload.name().orElse(response.name()),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
