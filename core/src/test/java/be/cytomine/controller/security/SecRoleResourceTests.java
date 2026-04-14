package be.cytomine.controller.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class SecRoleResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restSecRoleControllerMockMvc;

    @Test
    @Transactional
    public void list_all_roles() throws Exception {
        restSecRoleControllerMockMvc.perform(get("/api/role.json"))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void shouldSuccessfullyGetRole() throws Exception {
        restSecRoleControllerMockMvc.perform(get(
                "/api/role/{id}.json",
                builder.givenAUserRole().getSecRole().getId()
            ))
            .andExpect(status().isOk());
    }
}
