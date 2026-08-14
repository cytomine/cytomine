package be.cytomine.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.JsonObject;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPER_ADMIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class TaskControllerTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restCommandControllerMockMvc;

    @Test
    @Transactional
    @WithMockUser(username = SUPER_ADMIN)
    public void taskWorkflow() throws Exception {
        Project project = builder.givenAProject();
        MvcResult response = restCommandControllerMockMvc.perform(post("/api/task.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonObject.of("project", project.getId()).toJsonString()))
            .andExpect(status().isOk()).andReturn();

        JsonObject responseObject = JsonObject.toJsonObject(response.getResponse().getContentAsString());
        Integer id = (Integer) ((Map<String, Object>) responseObject.get("task")).get("id");

        restCommandControllerMockMvc.perform(get("/api/task/{id}.json", id))
            .andExpect(status().isOk());

        restCommandControllerMockMvc.perform(get("/api/project/{project}/task/comment.json", project.getId()))
            .andExpect(status().isOk());
    }
}
