package be.cytomine.service.social;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentUserPosition;
import be.cytomine.dto.image.AreaDTO;
import be.cytomine.repositorynosql.social.PersistentImageConsultationRepository;
import be.cytomine.service.image.SliceCoordinatesService;
import be.cytomine.utils.JsonObject;

import static be.cytomine.service.social.UserPositionServiceTests.USER_VIEW;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ImageConsultationServiceTests {

    @Autowired
    ImageConsultationService imageConsultationService;

    @Autowired
    PersistentImageConsultationRepository persistentImageConsultationRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    UserPositionService userPositionService;

    @Autowired
    SliceCoordinatesService sliceCoordinatesService;

    @BeforeEach
    public void cleanDB() {
        persistentImageConsultationRepository.deleteAll();
    }

    PersistentImageConsultation givenAPersistentImageConsultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        givenAPersistentUserPosition(
            created,
            user,
            sliceCoordinatesService.getReferenceSlice(imageInstance),
            USER_VIEW
        );
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }

    PersistentUserPosition givenAPersistentUserPosition(
        Date creation,
        User user,
        SliceInstance sliceInstance,
        AreaDTO areaDTO
    ) {
        return userPositionService.add(
            creation,
            user,
            sliceInstance,
            sliceInstance.getImage(),
            areaDTO,
            1,
            5.0,
            false
        );
    }

    @Test
    void creationAndClose() {
        User user = builder.givenSuperAdmin();
        ImageInstance imageInstance = builder.givenASliceInstance().getImage();
        PersistentImageConsultation consultation = givenAPersistentImageConsultation(
            user,
            imageInstance,
            new Date()
        );
        AssertionsForClassTypes.assertThat(consultation).isNotNull();
        AssertionsForClassTypes.assertThat(consultation.getTime()).isNull();
        Date after = new Date();

        consultation = givenAPersistentImageConsultation(user, imageInstance, new Date());


        Optional<PersistentImageConsultation>
            connectionOptional
            = persistentImageConsultationRepository.findAllByUserAndImageAndCreatedLessThan(
            builder.givenSuperAdmin().getId(), imageInstance.getId(), after,
            PageRequest.of(0, 1, Sort.Direction.DESC, "created")
        ).stream().findFirst();
        assertThat(connectionOptional).isPresent();
        AssertionsForClassTypes.assertThat(connectionOptional.get().getSession()).isEqualTo("xxx");
        AssertionsForClassTypes.assertThat(connectionOptional.get().getTime()).isEqualTo(0);
    }


    @Test
    void fillProjectConnectionUpdateAnnotationsCounter() {
        User user = builder.givenSuperAdmin();
        Project projet = builder.givenAProject();
        ImageInstance imageInstance = builder.givenASliceInstance(projet).getImage();

        PersistentImageConsultation consultation = givenAPersistentImageConsultation(
            user,
            imageInstance,
            DateUtils.addSeconds(new Date(), -10)
        );
        assertThat(consultation.getCountCreatedAnnotations()).isNull();

        UserAnnotation annotation = builder.givenANotPersistedUserAnnotation(projet);
        annotation.setImage(imageInstance);
        builder.persistAndReturn(annotation);

        consultation = givenAPersistentImageConsultation(user, imageInstance, DateUtils.addSeconds(new Date(), 1));
        consultation = givenAPersistentImageConsultation(user, imageInstance, DateUtils.addSeconds(new Date(), 10));
        Page<PersistentImageConsultation> allByUserAndProject =
            persistentImageConsultationRepository.findAllByProjectAndUser(
                projet.getId(),
                user.getId(),
                PageRequest.of(0, 50, Sort.Direction.DESC, "created")
            );

        for (PersistentImageConsultation c : allByUserAndProject) {
            System.out.println("Annotations: " + c.getCountCreatedAnnotations());
        }
        assertThat(allByUserAndProject.getTotalElements()).isEqualTo(3);
        assertThat(allByUserAndProject.getContent().get(0).getCountCreatedAnnotations()).isNull();
        assertThat(allByUserAndProject.getContent().get(1).getCountCreatedAnnotations()).isEqualTo(0);
        assertThat(allByUserAndProject.getContent().get(2).getCountCreatedAnnotations()).isEqualTo(1);
    }

    @Test
    void listImageConsultationByProjectAndUserDoNotDistinctImage() {
        User user = builder.givenSuperAdmin();
        ImageInstance imageInstance = builder.givenASliceInstance().getImage();

        givenAPersistentImageConsultation(user, imageInstance, new Date());

        Page<PersistentImageConsultation>
            results
            = imageConsultationService.listImageConsultationByProjectAndUserNoImageDistinct(
            imageInstance.getProject(),
            user,
            0,
            0
        );
        assertThat(results).hasSize(1);
    }

    @Test
    void listImageConsultationByProjectAndUserWithDistinctImage() {
        User user = builder.givenSuperAdmin();
        ImageInstance imageInstance1 = builder.givenASliceInstance().getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(imageInstance1.getProject()).getImage();

        givenAPersistentImageConsultation(user, imageInstance1, new Date());
        givenAPersistentImageConsultation(user, imageInstance1, new Date());


        List<JsonObject> results = imageConsultationService.listImageConsultationByProjectAndUserWithDistinctImage(
            imageInstance1.getProject(), user);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("imageName")).isEqualTo(imageInstance1.getBlindInstanceFilename());

        givenAPersistentImageConsultation(user, imageInstance2, new Date());

        results = imageConsultationService.listImageConsultationByProjectAndUserWithDistinctImage(
            imageInstance1.getProject(), user);
        assertThat(results).hasSize(2);

    }

    @Test
    void listImageOfUsersByProject() {
        User user1 = builder.givenSuperAdmin();
        User user2 = builder.givenAUser();

        ImageInstance imageInstance1 = builder.givenASliceInstance().getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(imageInstance1.getProject()).getImage();

        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -3));
        givenAPersistentImageConsultation(user1, imageInstance2, DateUtils.addDays(new Date(), -2));
        givenAPersistentImageConsultation(user2, imageInstance1, DateUtils.addDays(new Date(), -1));

        List<JsonObject> results = imageConsultationService.lastImageOfUsersByProject(
            imageInstance1.getProject(),
            List.of(user1.getId(), user2.getId()),
            "created", "desc", 0L, 0L
        );

        System.out.println(results);
        assertThat(results).hasSize(2);

        assertThat(results.get(0).get("user")).isEqualTo(user2.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance1.getId());
        assertThat(results.get(1).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(1).get("image")).isEqualTo(imageInstance2.getId());


        results = imageConsultationService.lastImageOfUsersByProject(
            imageInstance1.getProject(),
            List.of(user1.getId()),
            "created", "desc", 0L, 0L
        );
        assertThat(results).hasSize(1);

        results = imageConsultationService.lastImageOfUsersByProject(
            imageInstance1.getProject(),
            null,
            "created", "desc", 0L, 0L
        );
        assertThat(results).hasSize(2);

        results = imageConsultationService.lastImageOfUsersByProject(
            imageInstance1.getProject(),
            null,
            "created", "desc", 1L, 0L
        );
        assertThat(results).hasSize(1);

    }


    @Test
    void shouldReturnLastConsultedImagePerUserForProject() {
        User user1 = builder.givenSuperAdmin();
        User user2 = builder.givenAUser();
        User userWithNoConsultation = builder.givenAUser();

        ImageInstance imageInstance1 = builder.givenASliceInstance().getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(imageInstance1.getProject()).getImage();

        List<JsonObject> results = imageConsultationService.lastImageOfGivenUsersByProject(
            imageInstance1.getProject(),
            List.of(user1.getId(), user2.getId()),
            "created",
            "desc",
            0L,
            0L
        );
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("image")).isNull();
        assertThat(results.get(1).get("image")).isNull();

        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -3));
        givenAPersistentImageConsultation(user1, imageInstance2, DateUtils.addDays(new Date(), -2));
        givenAPersistentImageConsultation(user2, imageInstance1, DateUtils.addDays(new Date(), -1));

        results = imageConsultationService.lastImageOfGivenUsersByProject(
            imageInstance1.getProject(),
            List.of(user1.getId(), user2.getId(), userWithNoConsultation.getId()),
            "created",
            "desc",
            0L,
            0L
        );
        assertThat(results).hasSize(3);
        assertThat(results.get(0).get("user")).isEqualTo(user2.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance1.getId());
        assertThat(results.get(1).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(1).get("image")).isEqualTo(imageInstance2.getId());
        assertThat(results.get(2).get("user")).isEqualTo(userWithNoConsultation.getId());
        assertThat(results.get(2).get("image")).isNull();

        results = imageConsultationService.lastImageOfGivenUsersByProject(
            imageInstance1.getProject(),
            List.of(user1.getId(), user2.getId()),
            "created",
            "desc",
            1L,
            0L
        );
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("user")).isEqualTo(user2.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance1.getId());
    }

    @Test
    void shouldReturnUserImagesConsultedWithinDateRange() {
        User user1 = builder.givenSuperAdmin();
        User user2 = builder.givenAUser();

        ImageInstance imageInstance1 = builder.givenASliceInstance().getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(imageInstance1.getProject()).getImage();

        List<JsonObject> results = imageConsultationService.getImagesOfUsersByProjectBetween(
            user1.getId(),
            imageInstance1.getProject().getId(),
            null,
            null
        );
        assertThat(results).hasSize(0);

        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -10));
        givenAPersistentImageConsultation(user1, imageInstance2, DateUtils.addDays(new Date(), -5));
        givenAPersistentImageConsultation(user2, imageInstance1, DateUtils.addDays(new Date(), -1));

        results = imageConsultationService.getImagesOfUsersByProjectBetween(
            user1.getId(),
            imageInstance1.getProject().getId(),
            DateUtils.addDays(new Date(), -20),
            DateUtils.addDays(new Date(), 10)
        );
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance2.getId());
        assertThat(results.get(1).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(1).get("image")).isEqualTo(imageInstance1.getId());

        results = imageConsultationService.getImagesOfUsersByProjectBetween(
            user1.getId(),
            imageInstance1.getProject().getId(),
            DateUtils.addDays(new Date(), -6),
            DateUtils.addDays(new Date(), 10)
        );
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance2.getId());

        results = imageConsultationService.getImagesOfUsersByProjectBetween(
            user1.getId(),
            imageInstance1.getProject().getId(),
            DateUtils.addDays(new Date(), -20),
            DateUtils.addDays(new Date(), -6)
        );
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("user")).isEqualTo(user1.getId());
        assertThat(results.get(0).get("image")).isEqualTo(imageInstance1.getId());
    }

    @Test
    void shouldReturnConsultationFrequencyPerImageForUserInProject() {
        User user1 = builder.givenSuperAdmin();
        User user2 = builder.givenAUser();

        ImageInstance imageInstance1 = builder.givenASliceInstance().getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(imageInstance1.getProject()).getImage();

        List<JsonObject> results = imageConsultationService.getImagesOfUsersByProjectBetween(
            user1.getId(),
            imageInstance1.getProject().getId(),
            null,
            null
        );
        assertThat(results).hasSize(0);

        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -10));
        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -7));
        givenAPersistentImageConsultation(user1, imageInstance2, DateUtils.addDays(new Date(), -5));
        givenAPersistentImageConsultation(user2, imageInstance1, DateUtils.addDays(new Date(), -1));

        results = imageConsultationService.resumeByUserAndProject(user1.getId(), imageInstance1.getProject().getId());

        assertThat(results).hasSize(2);

        Optional<JsonObject> user1image1 = results.stream()
            .filter(x -> x.get("user").equals(user1.getId()) && x.get("image").equals(imageInstance1.getId()))
            .findFirst();
        assertThat(user1image1).isPresent();
        assertThat(user1image1.get().get("frequency")).isEqualTo(2);

        Optional<JsonObject> user1image2 = results.stream()
            .filter(x -> x.get("user").equals(user1.getId()) && x.get("image").equals(imageInstance2.getId()))
            .findFirst();
        assertThat(user1image2).isPresent();
        assertThat(user1image2.get().get("frequency")).isEqualTo(1);
    }

    @Test
    void totalNumberOfConsultationByProjectWithDates() {
        Project projet = builder.givenAProject();
        User user1 = builder.givenSuperAdmin();
        User anotherUser = builder.givenAUser();

        ImageInstance imageInstance1 = builder.givenASliceInstance(projet).getImage();
        ImageInstance imageInstance2 = builder.givenASliceInstance(projet).getImage();

        Date noConnectionBefore = DateUtils.addDays(new Date(), -100);
        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -10));
        givenAPersistentImageConsultation(user1, imageInstance1, DateUtils.addDays(new Date(), -10));
        Date twoConnectionBefore = DateUtils.addDays(new Date(), -5);
        givenAPersistentImageConsultation(anotherUser, imageInstance1, DateUtils.addDays(new Date(), -1));
        Date threeConnectionBefore = new Date();

        List<JsonObject> results;

        AssertionsForClassTypes.assertThat(imageConsultationService.countByProject(projet, null, null))
            .isEqualTo(3);
        AssertionsForClassTypes.assertThat(imageConsultationService.countByProject(
                projet,
                noConnectionBefore.getTime(),
                twoConnectionBefore.getTime()
            ))
            .isEqualTo(2);
        AssertionsForClassTypes.assertThat(imageConsultationService.countByProject(
                projet,
                twoConnectionBefore.getTime(),
                threeConnectionBefore.getTime()
            ))
            .isEqualTo(1);
    }
}
