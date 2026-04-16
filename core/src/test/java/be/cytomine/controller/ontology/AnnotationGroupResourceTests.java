package be.cytomine.controller.ontology;

import java.util.UUID;

import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
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
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.project.Project;
import be.cytomine.service.ontology.AnnotationGroupService;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class AnnotationGroupResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restAnnotationGroupControllerMockMvc;

    @Autowired
    private AnnotationGroupService annotationGroupService;

    @Test
    void findAnnotationGroupWithSuccess() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        AssertionsForClassTypes.assertThat(annotationGroupService.find(annotationGroup.getId()).isPresent());
        assertThat(annotationGroup).isEqualTo(annotationGroupService.find(annotationGroup.getId()).get());
    }

    @Test
    void findNonExistingAnnotationGroupReturnEmpty() {
        AssertionsForClassTypes.assertThat(annotationGroupService.find(0L)).isEmpty();
    }

    @Test
    @Transactional
    public void addValidAnnotationGroup() throws Exception {
        AnnotationGroup annotationGroup = builder.givenANotPersistedAnnotationGroup();
        restAnnotationGroupControllerMockMvc.perform(post("/api/annotationgroup.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(annotationGroup.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationgroupID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddAnnotationGroupCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationgroup.id").exists())
            .andExpect(jsonPath("$.annotationgroup.imageGroup").exists());
    }

    @Test
    @Transactional
    public void editValidAnnotationGroup() throws Exception {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        JsonObject jsonObject = annotationGroup.toJsonObject();
        String type = UUID.randomUUID().toString();
        jsonObject.put("type", type);
        restAnnotationGroupControllerMockMvc.perform(put("/api/annotationgroup/{id}.json", annotationGroup.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJsonString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationgroupID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditAnnotationGroupCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationgroup.id").exists())
            .andExpect(jsonPath("$.annotationgroup.type").value(type));
    }

    @Test
    @Transactional
    public void deleteAnnotationGroup() throws Exception {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        restAnnotationGroupControllerMockMvc.perform(delete("/api/annotationgroup/{id}.json", annotationGroup.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationgroupID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteAnnotationGroupCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationgroup.id").exists());
    }

    @Test
    @Transactional
    public void listAnnotationGroupByProject() throws Exception {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        restAnnotationGroupControllerMockMvc.perform(get(
                "/api/project/{id}/annotationgroup.json",
                annotationGroup.getProject().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + annotationGroup.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void listAnnotationGroupByImageGroup() throws Exception {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        restAnnotationGroupControllerMockMvc.perform(get(
                "/api/imagegroup/{id}/annotationgroup.json",
                annotationGroup.getImageGroup().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.id==" + annotationGroup.getId() + ")]").exists());
    }

    @Test
    @Transactional
    public void mergeAnnotationGroupWithSuccess() throws Exception {
        Project project = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project);
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroupToMerge = builder.givenAnAnnotationGroup(project, imageGroup);
        restAnnotationGroupControllerMockMvc.perform(post(
                "/api/annotationgroup/{id}/annotationgroup/{mergedId}/merge.json",
                annotationGroup.getId(),
                annotationGroupToMerge.getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.annotationgroupID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditAnnotationGroupCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.annotationgroup.id").exists())
            .andExpect(jsonPath("$.annotationgroup.id").value(annotationGroup.getId()));
    }
}
