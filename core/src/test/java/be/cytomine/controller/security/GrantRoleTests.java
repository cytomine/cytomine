package be.cytomine.controller.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;

import static be.cytomine.authorization.AbstractAuthorizationTest.ADMIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = ADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class GrantRoleTests {

    protected MockHttpSession session;
    @Autowired
    private MockMvc restGrandRoleControllerMockMvc;

    @Test
    @WithMockUser(username = ADMIN)
    public void openCloseAdminSessionAsAdmin() throws Exception {
        startSession();

        restGrandRoleControllerMockMvc.perform(get("/session/admin/info.json")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adminByNow").value(false))
            .andExpect(jsonPath("$.userByNow").value(true));

        restGrandRoleControllerMockMvc.perform(get("/session/admin/open.json")
                .session(session))
            .andExpect(status().isOk());

        restGrandRoleControllerMockMvc.perform(get("/session/admin/info.json")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adminByNow").value(true))
            .andExpect(jsonPath("$.userByNow").value(true));

        restGrandRoleControllerMockMvc.perform(get("/session/admin/close.json")
                .session(session))
            .andExpect(status().isOk());

        restGrandRoleControllerMockMvc.perform(get("/session/admin/info.json")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adminByNow").value(false))
            .andExpect(jsonPath("$.userByNow").value(true));

        endSession();
    }

    protected void startSession() {
        session = new MockHttpSession();
    }

    protected void endSession() {
        session.clearAttributes();
        session = null;
    }
}
