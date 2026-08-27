package be.cytomine.controller.ontology;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationIndex;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.annotation.AnnotationIndexLightDTO;
import be.cytomine.repository.ontology.AnnotationIndexRepository;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = SUPERADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class AnnotationIndexResourceTests {

    Project project;
    ImageInstance image;
    SliceInstance slice;
    UserResponse me;
    Term term;
    UserAnnotation a1;
    UserAnnotation a2;
    UserAnnotation a3;
    UserAnnotation a4;
    @Autowired
    private EntityManager em;
    @Autowired
    private BasicInstanceBuilder builder;
    @Autowired
    private MockMvc restAnnotationIndexControllerMockMvc;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private AnnotationIndexRepository annotationIndexRepository;

    void createAnnotationSet() throws ParseException {
        project = builder.givenAProject();
        image = builder.givenAnImageInstance(project);
        slice = builder.givenASliceInstance(image, 0, 0, 0);
        me = builder.givenSuperAdmin();
        term = builder.givenATerm(project.getOntology());

        a1 = builder.givenAUserAnnotation(slice, "POLYGON((1 1,5 1,5 5,1 5,1 1))", me, term);
        a2 = builder.givenAUserAnnotation(slice, "POLYGON((1 1,5 1,5 5,1 5,1 1))", me, term);
        a3 = builder.givenAUserAnnotation(slice, "POLYGON((1 1,5 1,5 5,1 5,1 1))", me, term);

        a4 = builder.givenAUserAnnotation(slice, "POLYGON((1 1,5 1,5 5,1 5,1 1))", me, null);
        em.flush();
    }

    @BeforeEach
    public void beforeEach() {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    createAnnotationSet();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void listUserAnnotationPropertyShow() throws Exception {
        List<AnnotationIndex> all = annotationIndexRepository.findAll();
        List<AnnotationIndexLightDTO> slices = annotationIndexRepository.findAllBySlice(slice);
        List<AnnotationIndexLightDTO> slicesLight = annotationIndexRepository.findAllLightBySliceInstance(
            slice.getId()
        );
        restAnnotationIndexControllerMockMvc.perform(get("/api/sliceinstance/{id}/annotationindex.json", slice.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.user==" + me.id() + ")]").exists())
            .andExpect(jsonPath("$.collection[?(@.user==" + me.id() + ")].slice").value(slice.getId().intValue()))
            .andExpect(jsonPath("$.collection[?(@.user==" + me.id() + ")].countAnnotation").value(4))
            .andExpect(jsonPath("$.collection[?(@.user==" + me.id() + ")].countReviewedAnnotation").value(0));
    }
}
