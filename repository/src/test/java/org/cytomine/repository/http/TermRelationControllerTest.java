package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRelationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class TermRelationControllerTest
    implements CRUDCommandTests<CreateTermRelation, TermRelationResponse, UpdateTermRelation> {

    String apiURL = TermRelationHttpContract.ROOT_PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermRelationRepository termRelationRepository;

    @Autowired
    private CommandV2Repository commandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateTermRelation createPayload;
    private UpdateTermRelation updatePayload;

    @Override
    public void beforeCreate(long userId) {
        long ontologyId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update("INSERT INTO ontology (id, version, name, user_id) VALUES (?, 0, ?, ?)", ontologyId,
            UUID.randomUUID().toString(), userId);
        long termId1 = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        long termId2 = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);


        jdbcTemplate.update("INSERT INTO term (id, name, color,version,ontology_id) VALUES (?, ?, ?,0,?);", termId1,
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), ontologyId);
        jdbcTemplate.update("INSERT INTO term (id, name, color,version,ontology_id) VALUES (?, ?, ?,0,?);", termId2,
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), ontologyId);
        createPayload = new CreateTermRelation(termId1, termId2, "parent");
        updatePayload = new UpdateTermRelation(Optional.of(termId1), Optional.of(termId2), Optional.of(1L));
    }


    @Override
    public TermRelationResponse expectedUpdatedResponse(TermRelationResponse response, UpdateTermRelation updatePayload,
                                                        LocalDateTime updatedTime) {
        return new TermRelationResponse(response.id(), updatePayload.term1Id().orElse(response.term1Id()),
            updatePayload.term2Id().orElse(response.term2Id()),
            updatePayload.relationId().orElse(response.relationId()), updatedTime, response.deleted(),
            response.created(), response.name());
    }

    @Override
    public TermRelationResponse expectedDeletedResponse(TermRelationResponse response, LocalDateTime deletedTime) {
        return new TermRelationResponse(response.id(), response.term1Id(), response.term2Id(), response.relationId(),
            response.updated(), Optional.of(deletedTime), response.created(), response.name());
    }

    @Override
    public TermRelationResponse expectChangedUpdatedTime(TermRelationResponse response, LocalDateTime updatedTime) {
        return new TermRelationResponse(response.id(), response.term1Id(), response.term2Id(), response.relationId(),
            updatedTime, response.deleted(), response.created(), response.name());
    }
}
