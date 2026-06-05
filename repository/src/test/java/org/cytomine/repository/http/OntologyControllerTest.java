package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.OntologyMapper;
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

    String apiURL = OntologyHttpContract.ROOT_PATH;
    CreateOntology createPayload = new CreateOntology(UUID.randomUUID().toString());
    UpdateOntology updatePayload = new UpdateOntology(Optional.of(UUID.randomUUID().toString()));

    @Override
    public OntologyResponse expectedUpdatedResponse(OntologyResponse response, UpdateOntology updatePayload,
        LocalDateTime updated) {
        return new OntologyResponse(updatePayload.name().orElse(response.name()), response.id(),
            response.terms(), response.created(), updated, response.deleted());
    }

    @Override
    public OntologyResponse expectedDeletedResponse(OntologyResponse response, LocalDateTime deletedTime) {
        return new OntologyResponse(response.name(), response.id(), response.terms(), response.created(),
            response.updated(), Optional.of(deletedTime));
    }
}
