package be.cytomine.controller.ontology;

import jakarta.transaction.Transactional;
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
import be.cytomine.domain.ontology.AnnotationLink;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class AnnotationLinkResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restAnnotationLinkControllerMockMvc;

    @Test
    @Transactional
    public void addValidAnnotationLink() throws Exception {
        AnnotationLink annotationLink = builder.givenANotPersistedAnnotationLink();
        restAnnotationLinkControllerMockMvc.perform(post("/api/annotationlink.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(annotationLink.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationlinkID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddAnnotationLinkCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationlink.id").exists())
            .andExpect(jsonPath("$.annotationlink.group").exists());
    }

    @Test
    @Transactional
    public void listAnnotationLinkByAnnotationGroup() throws Exception {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        restAnnotationLinkControllerMockMvc.perform(get(
                "/api/annotationgroup/{id}/annotationlink.json",
                annotationLink.getGroup().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + annotationLink.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void listAnnotationLinkByAnnotation() throws Exception {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        restAnnotationLinkControllerMockMvc.perform(get(
                "/api/annotation/{id}/annotationlink.json",
                annotationLink.getAnnotationIdent()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + annotationLink.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void deleteAnnotationLink() throws Exception {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        restAnnotationLinkControllerMockMvc.perform(delete(
                "/api/annotationgroup/{annotationGroup}/annotation/{annotation}.json",
                annotationLink.getGroup().getId(),
                annotationLink.getAnnotationIdent()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationlinkID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteAnnotationLinkCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationlink.id").exists());
    }
}
