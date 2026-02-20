package be.cytomine.service.social;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.PostGisTestConfiguration;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.AnnotationAction;
import be.cytomine.repositorynosql.social.AnnotationActionRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class AnnotationActionServiceTests {

    @Autowired
    AnnotationActionRepository annotationActionRepository;

    @Autowired
    AnnotationActionService annotationActionService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void cleanDB() {
        annotationActionRepository.deleteAll();
    }


    AnnotationAction given_a_persistent_annotation_action(Date creation, AnnotationDomain annotationDomain, User user, String action) {
        AnnotationAction annotationAction =
                annotationActionService.add(
                        annotationDomain,
                        user,
                        action,
                        creation
                );
        return annotationAction;
    }

    @Test
    public void add_action_for_annotation() {
        assertThat(annotationActionRepository.count()).isEqualTo(0);
        given_a_persistent_annotation_action(new Date(), builder.given_a_user_annotation(), builder.given_superadmin(), "view");
        assertThat(annotationActionRepository.count()).isEqualTo(1);
    }

    @Test
    public void list_by_slice() {
        AnnotationDomain annotationDomain = builder.given_a_user_annotation();

        assertThat(annotationActionService.list(annotationDomain.getSlice(), null,null,null))
                .hasSize(0);

        given_a_persistent_annotation_action(new Date(),annotationDomain,builder.given_superadmin(),  "view");
        given_a_persistent_annotation_action(new Date(), annotationDomain, builder.given_superadmin(), "select");

        assertThat(annotationActionService.list(annotationDomain.getSlice(), null,null,null))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getSlice(), builder.given_superadmin(),null,null))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getSlice(), builder.given_a_user(),null,null))
                .hasSize(0);

        assertThat(annotationActionService.list(annotationDomain.getSlice(), builder.given_superadmin(), null,new Date().getTime()))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getSlice(), builder.given_superadmin(), new Date().getTime(),null))
                .hasSize(0);
    }

    @Test
    public void list_by_image() {
        AnnotationDomain annotationDomain = builder.given_a_user_annotation();

        assertThat(annotationActionService.list(annotationDomain.getImage(), null,null,null))
                .hasSize(0);

        given_a_persistent_annotation_action(new Date(),annotationDomain,builder.given_superadmin(),  "view");
        given_a_persistent_annotation_action(new Date(),annotationDomain, builder.given_superadmin(), "select");

        assertThat(annotationActionRepository.count()).isEqualTo(2);
        System.out.println(annotationActionRepository.findAll());
        assertThat(annotationActionService.list(annotationDomain.getImage(), null,null,null))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getImage(), builder.given_superadmin(),null,null))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getImage(), builder.given_a_user(),null,null))
                .hasSize(0);

        assertThat(annotationActionService.list(annotationDomain.getImage(), builder.given_superadmin(), null,new Date().getTime()))
                .hasSize(2);

        assertThat(annotationActionService.list(annotationDomain.getImage(), builder.given_superadmin(), new Date().getTime(),null))
                .hasSize(0);
    }


    @Test
    void total_annotation_action_by_project_count() {
        User user1 = builder.given_superadmin();
        AnnotationDomain annotationDomain = builder.given_a_user_annotation();

        Date noConnectionBefore = DateUtils.addDays(new Date(), -100);
        given_a_persistent_annotation_action(DateUtils.addDays(new Date(), -10), annotationDomain, user1, "select");
        given_a_persistent_annotation_action(DateUtils.addDays(new Date(), -10), annotationDomain, user1, "view");
        Date twoConnectionBefore = DateUtils.addDays(new Date(), -5);
        given_a_persistent_annotation_action(DateUtils.addDays(new Date(), -3), annotationDomain, user1, "select");
        Date threeConnectionBefore = new Date();

        List<Map<String, Object>> results;

        AssertionsForClassTypes.assertThat(annotationActionService.countByProject(annotationDomain.getProject(), null, null))
                .isEqualTo(3);
        AssertionsForClassTypes.assertThat(annotationActionService.countByProject(annotationDomain.getProject(), noConnectionBefore.getTime(), twoConnectionBefore.getTime()))
                .isEqualTo(2);
        AssertionsForClassTypes.assertThat(annotationActionService.countByProject(annotationDomain.getProject(), twoConnectionBefore.getTime(), threeConnectionBefore.getTime()))
                .isEqualTo(1);
    }

}
