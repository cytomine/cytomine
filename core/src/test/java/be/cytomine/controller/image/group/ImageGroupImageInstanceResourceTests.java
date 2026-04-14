package be.cytomine.controller.image.group;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;
import be.cytomine.domain.project.Project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ImageGroupImageInstanceResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restImageGroupImageInstanceControllerMockMvc;

    private static WireMockServer wireMockServer = new WireMockServer(8888);

    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    @Transactional
    public void listImagegroupImageinstanceByImageinstance() throws Exception {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        restImageGroupImageInstanceControllerMockMvc.perform(get(
                "/api/imageinstance/{id}/imagegroupimageinstance.json",
                igii.getImage().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + igii.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void addValidImagegroupImageinstance() throws Exception {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        restImageGroupImageInstanceControllerMockMvc.perform(post(
                "/api/imagegroup/{group}/imageinstance/{image}.json",
                igii.getGroup().getId(),
                igii.getImage().getId()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(igii.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.imagegroupimageinstanceID").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.imagegroupimageinstance.id").exists());
    }

    @Test
    @Transactional
    public void deleteImagegroupImageinstance() throws Exception {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        restImageGroupImageInstanceControllerMockMvc.perform(delete(
                "/api/imagegroup/{group}/imageinstance/{image}.json",
                igii.getGroup().getId(),
                igii.getImage().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.imagegroupimageinstanceID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteImageGroupImageInstanceCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.imagegroupimageinstance.id").exists());
    }

    @Test
    @Transactional
    public void getPreviousImagegroupImageinstance() throws Exception {
        Project project = builder.givenAProject();
        ImageGroup group = builder.givenAnImageGroup(project);
        ImageGroupImageInstance current = builder.givenAnImageGroupImageInstance(
            group,
            builder.givenAnImageInstance(project)
        );
        ImageGroupImageInstance previous = builder.givenAnImageGroupImageInstance(
            group,
            builder.givenAnImageInstance(project)
        );
        restImageGroupImageInstanceControllerMockMvc.perform(get(
                "/api/imagegroup/{group}/imageinstance/{image}/previous.json",
                current.getGroup().getId(),
                current.getImage().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(previous.getImage().getId()));
    }

    @Test
    @Transactional
    public void getNextImagegroupImageinstance() throws Exception {
        Project project = builder.givenAProject();
        ImageGroup group = builder.givenAnImageGroup(project);
        ImageGroupImageInstance current = builder.givenAnImageGroupImageInstance(
            group,
            builder.givenAnImageInstance(project)
        );
        ImageGroupImageInstance next = builder.givenAnImageGroupImageInstance(
            group,
            builder.givenAnImageInstance(project)
        );
        restImageGroupImageInstanceControllerMockMvc.perform(get(
                "/api/imagegroup/{group}/imageinstance/{image}/next.json",
                current.getGroup().getId(),
                current.getImage().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(next.getImage().getId()));
    }
}
