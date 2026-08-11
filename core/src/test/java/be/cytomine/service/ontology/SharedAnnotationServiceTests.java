package be.cytomine.service.ontology;

import java.util.List;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.SharedAnnotation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.repository.ontology.SharedAnnotationRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.UrlApi;
import be.cytomine.service.command.TransactionService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class SharedAnnotationServiceTests {

    @Autowired
    SharedAnnotationService sharedAnnotationService;

    @Autowired
    SharedAnnotationRepository sharedAnnotationRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;
    @Autowired
    private UrlApi urlApi;

    @Test
    void listAllSharedAnnotationWithSuccess() {
        SharedAnnotation sharedAnnotation = builder.givenASharedAnnotation();
        assertThat(sharedAnnotation).isIn(sharedAnnotationService.list());
    }

    @Test
    void getSharedAnnotationWithSuccess() {
        SharedAnnotation sharedAnnotation = builder.givenASharedAnnotation();
        assertThat(sharedAnnotation).isEqualTo(sharedAnnotationService.get(sharedAnnotation.getId()));
    }

    @Test
    void getUnexistingSharedAnnotationReturnNull() {
        assertThat(sharedAnnotationService.get(0L)).isNull();
    }

    @Test
    void findSharedAnnotationWithSuccess() {
        SharedAnnotation sharedAnnotation = builder.givenASharedAnnotation();
        assertThat(sharedAnnotationService.find(sharedAnnotation.getId()).isPresent());
        assertThat(sharedAnnotation).isEqualTo(sharedAnnotationService.find(sharedAnnotation.getId()).get());
    }

    @Test
    void findUnexistingSharedAnnotationReturnEmpty() {
        assertThat(sharedAnnotationService.find(0L)).isEmpty();
    }

    @Test
    void listAllSharedAnnotationByAnnotation() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        SharedAnnotation sharedAnnotation = builder.givenASharedAnnotation(annotation);
        assertThat(sharedAnnotation).isIn(sharedAnnotationService.listComments(annotation));
        assertThat(builder.givenASharedAnnotation()).isNotIn(sharedAnnotationService.listComments(annotation));
    }

    @Test
    void addValidSharedAnnotationWithSuccess() {
        AnnotationDomain annotationDomain = builder.givenAUserAnnotation();
        SharedAnnotation sharedAnnotation = builder.givenANotPersistedSharedAnnotation();
        sharedAnnotation.setAnnotation(annotationDomain);
        JsonObject json = sharedAnnotation.toJsonObject(urlApi);
        json.put("subject", "subject for test mail");
        json.put("message", "message for test mail");
        json.put("users", List.of(builder.givenSuperAdmin().getId()));
        json.put("annotationIdent", sharedAnnotation.getAnnotationIdent());
        json.put("annotationClassName", sharedAnnotation.getAnnotationClassName());

        CommandResponse commandResponse = sharedAnnotationService.add(json);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);

        assertThat(sharedAnnotationService.listComments(annotationDomain).size()).isEqualTo(1);

    }

    @Test
    void deleteSharedAnnotationWithSuccess() {
        SharedAnnotation sharedAnnotation = builder.givenASharedAnnotation();

        CommandResponse commandResponse = sharedAnnotationService.delete(sharedAnnotation, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(sharedAnnotationService.find(sharedAnnotation.getId()).isEmpty());
    }
}
