package org.cytomine.repository.http;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class OntologyControllerTest implements CRUDCommandTests<CreateOntology, OntologyResponse, UpdateOntology> {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    OntologyMapper ontologyController;

    @Autowired
    TermRepository termRepository;

    String apiURL = OntologyHttpContract.ROOT_PATH;
    CreateOntology createPayload = new CreateOntology(UUID.randomUUID().toString());
    UpdateOntology updatePayload = new UpdateOntology(Optional.of(UUID.randomUUID().toString()));

    @Override
    public void createSubEntities(long userId, long currentId) {
        termRepository.save(
            new TermEntity(null, 0, currentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                Timestamp.from(Instant.now()), null, null, "", Set.of()));
    }

    @Override
    public OntologyResponse expectedUpdatedResponse(OntologyResponse response, UpdateOntology updatePayload,
        LocalDateTime updated) {
        return new OntologyResponse(updatePayload.name().orElse(response.name()), response.id(), response.terms(),
            response.created(), Optional.of(updated), response.deleted(), response.user());
    }

    @Override
    public OntologyResponse expectedDeletedResponse(OntologyResponse response, LocalDateTime deletedTime) {
        return new OntologyResponse(response.name(), response.id(), response.terms(), response.created(),
            response.updated(), Optional.of(deletedTime), response.user());
    }

    @Override
    public OntologyResponse expectChangedUpdatedTime(OntologyResponse response, LocalDateTime updatedTime) {
        return new OntologyResponse(response.name(), response.id(), response.terms(), response.created(),
            Optional.of(updatedTime), response.deleted(), response.user());
    }
}
