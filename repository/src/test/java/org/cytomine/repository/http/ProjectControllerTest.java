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
import be.cytomine.common.repository.http.ProjectHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.project.payload.CreateProject;
import be.cytomine.common.repository.model.project.payload.UpdateProject;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import({PostGisTestConfiguration.class, SecurityMockMvcTestConfiguration.class})
@Getter
class ProjectControllerTest implements CRUDCommandTests<CreateProject, ProjectResponse, UpdateProject> {
    String apiURL = ProjectHttpContract.ROOT_PATH;
    CreateProject createPayload =
        new CreateProject(UUID.randomUUID().toString(), Optional.empty(), false, false, false, false, false, false,
            false);
    UpdateProject updatePayload =
        new UpdateProject(Optional.of(UUID.randomUUID().toString()), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ProjectResponse expectedUpdatedResponse(ProjectResponse response, UpdateProject updatePayload,
        LocalDateTime updatedTime) {
        return new ProjectResponse(response.id(), updatePayload.name().orElse(response.name()),
            updatePayload.ontology().or(response::ontology), response.ontologyName(),
            updatePayload.blindMode().orElse(response.blindMode()),
            updatePayload.areImagesDownloadable().orElse(response.areImagesDownloadable()), response.numberOfImages(),
            response.numberOfAnnotations(), response.numberOfJobAnnotations(), response.numberOfReviewedAnnotations(),
            updatePayload.isClosed().orElse(response.isClosed()), response.isReadOnly(), response.isRestricted(),
            updatePayload.hideUsersLayers().orElse(response.hideUsersLayers()),
            updatePayload.hideAdminsLayers().orElse(response.hideAdminsLayers()), response.created(),
            Optional.of(updatedTime), response.deleted());
    }
}
