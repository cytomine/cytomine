package org.cytomine.repository.http;

import lombok.SneakyThrows;
import org.cytomine.repository.RepositoryApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.RelationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Transactional
class RelationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void findParentRelationReturnsRelation() {
        String response = mockMvc.perform(get("/relation/parent"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        RelationResponse result = objectMapper.readValue(response, RelationResponse.class);
        assertEquals("parent", result.name());
    }
}
