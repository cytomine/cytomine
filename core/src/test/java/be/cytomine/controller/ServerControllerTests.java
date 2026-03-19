package be.cytomine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
public class ServerControllerTests {

    @Autowired
    private MockMvc restConfigurationControllerMockMvc;

    @Test
    public void pingShouldReturnOk() throws Exception {
        restConfigurationControllerMockMvc.perform(get("/server/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alive").value(true))
            .andExpect(jsonPath("$.version").hasJsonPath())
            .andExpect(jsonPath("$.serverURL").hasJsonPath())
            .andExpect(jsonPath("$.serverID").hasJsonPath());
    }

    @Test
    public void statusShouldReturnOk() throws Exception {
        restConfigurationControllerMockMvc.perform(get("/status.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alive").value(true))
            .andExpect(jsonPath("$.version").hasJsonPath())
            .andExpect(jsonPath("$.serverURL").hasJsonPath());
    }
}
