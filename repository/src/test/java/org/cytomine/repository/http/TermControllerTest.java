package org.cytomine.repository.http;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.cytomine.repository.persistence.OntologyRepository;
import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class TermControllerTest implements CRUDCommandTests<CreateTerm, TermResponse, UpdateTerm> {

    String apiURL = TermHttpContract.ROOT_PATH;

    CreateTerm createPayload;
    UpdateTerm updatePayload =
        new UpdateTerm(Optional.of(UUID.randomUUID().toString()), Optional.of(UUID.randomUUID().toString()));
    @Autowired
    private ApplyCommandResponseMapper applyCommandResponseMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OntologyRepository ontologyRepository;

    private long ontologyId;

    @Override
    public void beforeCreate(long userId) {
        ontologyId = ontologyRepository.save(
            new OntologyEntity(null, 0, UUID.randomUUID().toString(), userId, Timestamp.from(Instant.now()), null, null,
                Set.of())).getId();
        createPayload = new CreateTerm(UUID.randomUUID().toString(), UUID.randomUUID().toString(), ontologyId,
            Optional.of(UUID.randomUUID().toString()));
    }

    @Override
    public TermResponse expectedUpdatedResponse(TermResponse response, UpdateTerm updatePayload,
        LocalDateTime updatedTime) {
        return new TermResponse(response.id(), updatePayload.name().orElse(response.name()),
            updatePayload.color().orElse(response.color()), response.ontologyId(), response.created(),
            Optional.of(updatedTime), response.deleted(), response.comment(), response.children());
    }
}
