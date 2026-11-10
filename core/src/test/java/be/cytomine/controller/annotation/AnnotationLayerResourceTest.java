package be.cytomine.controller.annotation;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRunLayer;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@WithMockUser(username = SUPERADMIN)
@AutoConfigureMockMvc
@SpringBootTest
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class AnnotationLayerResourceTest {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private EntityManager manager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAnnotationLayersByImageShouldReturnAnnotationLayers() throws Exception {
        TaskRunLayer taskRunLayer = builder.givenAPersistedTaskRunLayer();

        mockMvc.perform(get("/api/image-instances/{id}/annotation-layers", taskRunLayer.getImage().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(taskRunLayer.getAnnotationLayer().getId()));
    }

    @Test
    public void getAnnotationLayersByImageShouldReturnAllLayersOfTaskRunWithSeveralOutputs() throws Exception {
        // An app with several geometry outputs creates one annotation layer per output for a single task run.
        // https://github.com/cytomine/cytomine/issues/1341: the repository must not assume a single layer per run.
        var project = builder.givenAProject();
        var image = builder.givenAnImageInstance(project);
        var taskRun = builder.givenANotPersistedTaskRun(project, java.util.UUID.randomUUID(), image);
        manager.persist(taskRun);
        AnnotationLayer firstLayer = builder.givenAPersistedAnnotationLayer();
        AnnotationLayer secondLayer = builder.givenAPersistedAnnotationLayer();
        manager.persist(builder.givenANotPersistedTaskRunLayer(firstLayer, taskRun, image));
        manager.persist(builder.givenANotPersistedTaskRunLayer(secondLayer, taskRun, image));
        manager.flush();

        mockMvc.perform(get("/api/image-instances/{id}/annotation-layers", image.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                firstLayer.getId().intValue(),
                secondLayer.getId().intValue()
            )));
    }

    @Test
    public void getAnnotationLayersByImageShouldNotReturnLayerOfLastTaskRunOnAnotherImage() throws Exception {
        // The last task run of the project ran on another image: its layer must not leak to this image.
        var project = builder.givenAProject();
        var imageOnOtherImage = builder.givenAnImageInstance(project);
        var queriedImage = builder.givenAnImageInstance(project);

        var taskRun = builder.givenANotPersistedTaskRun(
            project, java.util.UUID.randomUUID(), imageOnOtherImage);
        manager.persist(taskRun);
        manager.persist(builder.givenANotPersistedTaskRunLayer(
            builder.givenAPersistedAnnotationLayer(), taskRun, imageOnOtherImage));
        manager.flush();

        mockMvc.perform(get("/api/image-instances/{id}/annotation-layers", queriedImage.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getAnnotationsByLayerShouldReturnAnnotations() throws Exception {
        AnnotationLayer annotationLayer = builder.givenAPersistedAnnotationLayer();
        Annotation first = builder.givenANotPersistedAnnotation(annotationLayer);
        Annotation second = builder.givenANotPersistedAnnotation(annotationLayer);
        Annotation third = builder.givenANotPersistedAnnotation(annotationLayer);
        manager.persist(first);
        manager.persist(second);
        manager.persist(third);
        manager.flush();

        mockMvc.perform(get("/api/annotation-layers/{id}/annotations", annotationLayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath(
                "$[*].id", containsInAnyOrder(
                    first.getId().intValue(),
                    second.getId().intValue(),
                    third.getId().intValue()
                )
            ))
            .andExpect(jsonPath("$[*].annotationLayer.id", everyItem(is(annotationLayer.getId().intValue()))));
    }

    @Test
    public void getAnnotationsByLayerShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/annotation-layers/{id}/annotations", 42))
            .andExpect(status().isNotFound());
    }

    @Test
    public void findTaskRunLayerShouldReturnTaskRunLayer() throws Exception {
        TaskRunLayer taskRunLayer = builder.givenAPersistedTaskRunLayer();

        mockMvc.perform(get("/api/annotation-layers/{id}/task-run-layer", taskRunLayer.getAnnotationLayer().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.annotationLayer").value(taskRunLayer.getAnnotationLayer().getId()))
            .andExpect(jsonPath("$.taskRun").value(taskRunLayer.getTaskRun().getId()))
            .andExpect(jsonPath("$.image").value(taskRunLayer.getImage().getId()));
    }
}
