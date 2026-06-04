package org.cytomine.repository.http;

import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
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

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class OntologyControllerTest implements CRUDCommandTests<CreateOntology, OntologyResponse> {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    String apiURL = OntologyHttpContract.ROOT_PATH;
    CreateOntology createPayload = new CreateOntology(UUID.randomUUID().toString());
}
