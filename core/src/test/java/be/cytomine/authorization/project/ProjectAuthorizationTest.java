package be.cytomine.authorization.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.meta.AttachedFile;
import be.cytomine.domain.meta.Description;
import be.cytomine.domain.meta.Property;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.User;
import be.cytomine.repository.project.ProjectRepresentativeUserRepository;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.meta.AttachedFileService;
import be.cytomine.service.meta.DescriptionService;
import be.cytomine.service.meta.PropertyService;
import be.cytomine.service.meta.TagDomainAssociationService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.project.ProjectRepresentativeUserService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.search.ProjectSearchExtension;

import static be.cytomine.domain.project.EditingMode.CLASSIC;
import static be.cytomine.domain.project.EditingMode.READ_ONLY;
import static be.cytomine.domain.project.EditingMode.RESTRICTED;
import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ProjectAuthorizationTest extends CRUDAuthorizationTest {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private ProjectRepresentativeUserService projectRepresentativeUserService;

    @Autowired
    private ProjectRepresentativeUserRepository projectRepresentativeUserRepository;

    @Autowired
    private DescriptionService descriptionService;

    @Autowired
    private AttachedFileService attachedFileService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ImageInstanceService imageInstanceService;

    @Autowired
    private TagDomainAssociationService tagDomainAssociationService;

    @Autowired
    private UserAnnotationService userAnnotationService;

    private Project project = null;

    private static WireMockServer wireMockServer;

    private static void setupStub() {
        /* Simulate call to PIMS */
        wireMockServer.stubFor(WireMock.post(urlPathMatching(IMS_API_BASE_PATH + "/image/.*/annotation/drawing"))
            .withRequestBody(WireMock.matching(".*"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString().getBytes()))
        );

        /* Simulate call to CBIR */
        wireMockServer.stubFor(WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/images"))
            .withQueryParam("storage", WireMock.matching(".*"))
            .withQueryParam("index", WireMock.equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        wireMockServer.stubFor(WireMock.delete(urlPathMatching(CBIR_API_BASE_PATH + "/images/.*"))
            .withQueryParam("storage", WireMock.matching(".*"))
            .withQueryParam("index", WireMock.equalTo("annotation"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        wireMockServer.stubFor(WireMock.post(urlPathEqualTo(CBIR_API_BASE_PATH + "/storages"))
            .withRequestBody(WireMock.matching(".*"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );

        wireMockServer.stubFor(delete(urlPathEqualTo(CBIR_API_BASE_PATH + "/storages"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();

        setupStub();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void before() throws Exception {
        if (project == null) {
            project = builder.givenAProject();

            initACL(project);
        }
        project.setMode(CLASSIC);
        builder.persistAndReturn(project);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_projects() {
        assertThat(projectService.list(null, new ProjectSearchExtension(), new ArrayList<>(), "created", "desc", 0L, 0L)
            .stream().map(x -> x.get("id")))
            .contains(project.getId());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_at_least_read_permission_can_list_projects() {
        assertThat(projectService.list(
                (User) userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get(),
                new ProjectSearchExtension(),
                new ArrayList<>(),
                "created",
                "desc",
                0L,
                0L
            )
            .stream().map(x -> x.get("id")))
            .contains(project.getId());
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_projects() {
        expectForbidden(() -> {
            projectService.list(null, new ProjectSearchExtension(), new ArrayList<>(), "created", "desc", 0L, 0L)
                .stream()
                .map(x -> x.get("id"));
        });
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_add_user_to_project() {
        expectOK(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, true));
        expectOK(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_with_admin_rigth_can_manage_user_in_project() {
        expectOK(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, true));
        expectOK(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_acl_cannot_manage_user_in_project() {
        expectForbidden(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, true));
        expectForbidden(() -> projectMemberService.addUserToProject(builder.givenAUser(), project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void classic_project_scenario_for_admin() {

        expectOK(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectOK(() -> projectMemberService.addUserToProject(user, project, false));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, true));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project,
            user
        );

        // add another representative so that we can delete the first one
        expectOK(() -> projectMemberService.addUserToProject(builder.givenSuperAdmin(), project, false));
        expectOK(() -> projectRepresentativeUserService.add(builder.givenANotPersistedProjectRepresentativeUser(
            project, builder.givenSuperAdmin()
        ).toJsonObject()));

        expectOK(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        expectOK(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUserService.find(project, user).get(), null, null, false
        ));

        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(project, "xxxx", "xxxxxx")
            .toJsonObject()));
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            project
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void classic_project_scenario_for_user() {
        expectForbidden(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, false));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, true));

        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project, user
        );

        // add another representative so that we can try to delete the first one
        projectRepresentativeUserRepository.save(builder.givenANotPersistedProjectRepresentativeUser(
            project, builder.givenSuperAdmin()
        ));

        expectForbidden(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        builder.persistAndReturn(projectRepresentativeUser);
        expectForbidden(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUserService.find(project, user).get(), null, null, false
        ));

        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(
                project,
                UUID.randomUUID().toString(),
                "value"
            )
            .toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(builder.givenATag(), project)
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void classic_project_with_image_as_contributor() {
        //Force project to Read and write
        project.setMode(CLASSIC);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationAdmin, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(property, property.toJsonObject()));
        expectOK(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectOK(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionUser, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));

        //add,update, delete description (admin data)
        expectOK(() -> descriptionService.update(descriptionAdmin, descriptionAdmin.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionAdmin, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationAdmin)
            .toJsonObject()));

        //add,update, delete description (superadmin data)
        expectOK(() -> descriptionService.update(description, description.toJsonObject()));
        expectOK(() -> descriptionService.delete(description, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotation).toJsonObject()));

        //add,update, delete tagDomainAssociation (simple user data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationUser
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationAdmin
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotation
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectOK(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectOK(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectOK(() -> imageInstanceService.startReview(imageUser));
        expectOK(() -> imageInstanceService.stopReview(imageUser, false));

        //start reviewing image (admin data)
        expectOK(() -> imageInstanceService.startReview(imageAdmin));
        expectOK(() -> imageInstanceService.stopReview(imageAdmin, false));

        //start reviewing image (superadmin data)
        expectOK(() -> imageInstanceService.startReview(image));
        expectOK(() -> imageInstanceService.stopReview(image, false));

        //add annotation on my layer
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        //add annotation on other layers
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceUser).toJsonObject()));
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectOK(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectOK(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectOK(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectOK(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project).toJsonObject()));

        //update, delete image instance (simple user data)
        expectOK(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectOK(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectOK(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(image, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void classic_project_with_image_as_project_manager() {
        //Force project to Read and write
        project.setMode(CLASSIC);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationAdmin, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(property, property.toJsonObject()));
        expectOK(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectOK(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionUser, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));

        //add,update, delete description (admin data)
        expectOK(() -> descriptionService.update(descriptionAdmin, descriptionAdmin.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionAdmin, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationAdmin)
            .toJsonObject()));

        //add,update, delete description (superadmin data)
        expectOK(() -> descriptionService.update(description, description.toJsonObject()));
        expectOK(() -> descriptionService.delete(description, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotation).toJsonObject()));

        //add,update, delete tagDomainAssociation (simple user data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationUser
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationAdmin
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotation
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectOK(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectOK(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectOK(() -> imageInstanceService.startReview(imageUser));
        expectOK(() -> imageInstanceService.stopReview(imageUser, false));

        //start reviewing image (admin data)
        expectOK(() -> imageInstanceService.startReview(imageAdmin));
        expectOK(() -> imageInstanceService.stopReview(imageAdmin, false));

        //start reviewing image (superadmin data)
        expectOK(() -> imageInstanceService.startReview(image));
        expectOK(() -> imageInstanceService.stopReview(image, false));

        //add annotation on my layer
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        //add annotation on other layers
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceUser).toJsonObject()));
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectOK(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectOK(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectOK(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectOK(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project).toJsonObject()));

        //update, delete image instance (simple user data)
        expectOK(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectOK(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectOK(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(image, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void restricted_project_scenario_for_admin() {
        project.setMode(RESTRICTED);
        builder.persistAndReturn(project);
        expectOK(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectOK(() -> projectMemberService.addUserToProject(user, project, false));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, true));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project,
            user
        );

        // add another representative so that we can delete the first one
        expectOK(() -> projectMemberService.addUserToProject(builder.givenSuperAdmin(), project, false));
        expectOK(() -> projectRepresentativeUserService.add(builder.givenANotPersistedProjectRepresentativeUser(
            project, builder.givenSuperAdmin()
        ).toJsonObject()));

        expectOK(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        expectOK(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUserService.find(project, user).get(), null, null, false
        ));

        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(project, "xxxxxx", "yyy")
            .toJsonObject()));
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            project
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void restricted_project_scenario_for_user() {
        project.setMode(RESTRICTED);
        builder.persistAndReturn(project);

        expectForbidden(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, false));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, true));
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project,
            user
        );

        // add another representative so that we can try to delete the first one
        projectRepresentativeUserRepository.save(builder.givenANotPersistedProjectRepresentativeUser(
            project, builder.givenSuperAdmin()
        ));

        expectForbidden(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        builder.persistAndReturn(projectRepresentativeUser);
        expectForbidden(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUser, null, null, false
        ));

        //Description check if not readonly mode, other metadata stick to Write permission. To fix
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectForbidden(() -> propertyService.add(builder.givenAProperty(project).toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(builder.givenATag(), project)
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void restricted_project_with_image_as_contributor() {
        //Force project to Read and write
        project.setMode(RESTRICTED);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(
            annotationAdmin,
            "xxx",
            "value"
        ).toJsonObject()));
        expectForbidden(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectForbidden(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectForbidden(() -> propertyService.update(property, property.toJsonObject()));
        expectForbidden(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectOK(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionUser, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));
        //TODO description doesn't have a user or creator field. Doesn't check neither if admin or not so all is 200

        //add,update, delete tagDomainAssociation (simple user data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationUser
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationAdmin
        ).toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotation
        ).toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectOK(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectForbidden(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectForbidden(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectOK(() -> imageInstanceService.startReview(imageUser));

        //start reviewing image (admin data)
        expectForbidden(() -> imageInstanceService.startReview(imageAdmin));

        //start reviewing image (superadmin data)
        expectForbidden(() -> imageInstanceService.startReview(image));

        //add annotation on my layer
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(
            sliceUser,
            userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get()
        ).toJsonObject()));

        //add annotation on other layers
        expectForbidden(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        expectForbidden(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectOK(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectForbidden(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectForbidden(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectForbidden(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectForbidden(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectOK(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project).toJsonObject()));

        //update, delete image instance (simple user data)
        expectOK(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));

        expectOK(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectForbidden(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectForbidden(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectForbidden(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectForbidden(() -> imageInstanceService.delete(image, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void restricted_project_with_image_as_project_manager() {
        //Force project to Read and write
        project.setMode(RESTRICTED);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationAdmin, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(property, property.toJsonObject()));
        expectOK(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectOK(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionUser, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));

        //add,update, delete description (admin data)
        expectOK(() -> descriptionService.update(descriptionAdmin, descriptionAdmin.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionAdmin, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationAdmin)
            .toJsonObject()));

        //add,update, delete description (superadmin data)
        expectOK(() -> descriptionService.update(description, description.toJsonObject()));
        expectOK(() -> descriptionService.delete(description, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotation).toJsonObject()));

        //add,update, delete tagDomainAssociation (simple user data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationUser
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationAdmin
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotation
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectOK(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectOK(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectOK(() -> imageInstanceService.startReview(imageUser));
        expectOK(() -> imageInstanceService.stopReview(imageUser, false));

        //start reviewing image (admin data)
        expectOK(() -> imageInstanceService.startReview(imageAdmin));
        expectOK(() -> imageInstanceService.stopReview(imageAdmin, false));

        //start reviewing image (superadmin data)
        expectOK(() -> imageInstanceService.startReview(image));
        expectOK(() -> imageInstanceService.stopReview(image, false));

        //add annotation on my layer
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        //add annotation on other layers
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceUser).toJsonObject()));
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectOK(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectOK(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectOK(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectOK(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project).toJsonObject()));

        //update, delete image instance (simple user data)
        expectOK(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectOK(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectOK(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(image, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void readonly_project_scenario_for_admin() {
        project.setMode(READ_ONLY);
        builder.persistAndReturn(project);
        expectOK(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectOK(() -> projectMemberService.addUserToProject(user, project, false));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, true));
        expectOK(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project,
            user
        );

        // add another representative so that we can delete the first one
        expectOK(() -> projectMemberService.addUserToProject(builder.givenSuperAdmin(), project, false));
        expectOK(() -> projectRepresentativeUserService.add(builder.givenANotPersistedProjectRepresentativeUser(
            project, builder.givenSuperAdmin()
        ).toJsonObject()));

        expectOK(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        expectOK(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUserService.find(project, user).get(), null, null, false
        ));

        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectOK(() -> propertyService.add(builder.givenAProperty(project).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            project
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void readonly_project_scenario_for_user() {
        project.setMode(READ_ONLY);
        builder.persistAndReturn(project);

        expectForbidden(this::when_i_edit_domain);

        User user = builder.givenAUser();
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, false));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, false));
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, true));
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, true));

        ProjectRepresentativeUser projectRepresentativeUser = builder.givenANotPersistedProjectRepresentativeUser(
            project,
            user
        );

        // add another representative so that we can try to delete the first one
        projectRepresentativeUserRepository.save(builder.givenANotPersistedProjectRepresentativeUser(
            project,
            builder.givenSuperAdmin()
        ));

        expectForbidden(() -> projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()));
        builder.persistAndReturn(projectRepresentativeUser);
        expectForbidden(() -> projectRepresentativeUserService.delete(
            projectRepresentativeUser, null, null, false
        ));

        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(project).toJsonObject()));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
        expectForbidden(() -> propertyService.add(builder.givenAProperty(project).toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(builder.givenATag(), project)
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void read_only_project_with_image_as_contributor() {
        //Force project to Read and write
        project.setMode(READ_ONLY);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectForbidden(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectForbidden(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(annotationAdmin, "xxx", "value")
            .toJsonObject()));
        expectForbidden(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectForbidden(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectForbidden(() -> propertyService.update(property, property.toJsonObject()));
        expectForbidden(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));
        expectForbidden(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectForbidden(() -> descriptionService.delete(descriptionUser, null, null, false));

        //add,update, delete description (admin data)
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationAdmin)
            .toJsonObject()));
        expectForbidden(() -> descriptionService.update(descriptionAdmin, descriptionAdmin.toJsonObject()));
        expectForbidden(() -> descriptionService.delete(descriptionAdmin, null, null, false));

        //add,update, delete description (superadmin data)
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(annotation).toJsonObject()));
        expectForbidden(() -> descriptionService.update(description, description.toJsonObject()));
        expectForbidden(() -> descriptionService.delete(description, null, null, false));

        //add,update, delete tagDomainAssociation (simple user data)
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(
                builder.givenATag(),
                annotationUser
            )
            .toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(
                builder.givenATag(),
                annotationAdmin
            )
            .toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectForbidden(() -> tagDomainAssociationService.add(builder.givenATagAssociation(
                builder.givenATag(),
                annotation
            )
            .toJsonObject()));
        expectForbidden(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectForbidden(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectForbidden(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectForbidden(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectForbidden(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectForbidden(() -> imageInstanceService.startReview(imageUser));

        //start reviewing image (admin data)
        expectForbidden(() -> imageInstanceService.startReview(imageAdmin));

        //start reviewing image (superadmin data)
        expectForbidden(() -> imageInstanceService.startReview(image));

        //add annotation on my layer
        expectForbidden(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        //add annotation on other layers
        expectForbidden(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceUser).toJsonObject()));
        expectForbidden(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectForbidden(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectForbidden(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectForbidden(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectForbidden(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectForbidden(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectForbidden(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectForbidden(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project)
            .toJsonObject()));

        //update, delete image instance (simple user data)
        expectForbidden(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));
        expectForbidden(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectForbidden(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectForbidden(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectForbidden(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectForbidden(() -> imageInstanceService.delete(image, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void read_only_project_with_image_as_project_manager() {
        //Force project to Read and write
        project.setMode(RESTRICTED);
        builder.persistAndReturn(project);

        Map<String, Object> data = initProjectDataSet(project);

        //super admin data
        ImageInstance image = (ImageInstance) data.get("image");
        SliceInstance slice = (SliceInstance) data.get("slice");
        UserAnnotation annotation = (UserAnnotation) data.get("annotation");
        Description description = (Description) data.get("description");
        Property property = (Property) data.get("property");
        AttachedFile attachedFile = (AttachedFile) data.get("attachedFile");
        TagDomainAssociation tda = (TagDomainAssociation) data.get("tagDomainAssociation");

        //admin data
        ImageInstance imageAdmin = (ImageInstance) data.get("imageAdmin");
        SliceInstance sliceAdmin = (SliceInstance) data.get("sliceAdmin");
        UserAnnotation annotationAdmin = (UserAnnotation) data.get("annotationAdmin");
        Description descriptionAdmin = (Description) data.get("descriptionAdmin");
        Property propertyAdmin = (Property) data.get("propertyAdmin");
        AttachedFile attachedFileAdmin = (AttachedFile) data.get("attachedFileAdmin");
        TagDomainAssociation tdaAdmin = (TagDomainAssociation) data.get("tagDomainAssociationAdmin");

        //simple user data
        ImageInstance imageUser = (ImageInstance) data.get("imageUser");
        SliceInstance sliceUser = (SliceInstance) data.get("sliceUser");
        UserAnnotation annotationUser = (UserAnnotation) data.get("annotationUser");
        Description descriptionUser = (Description) data.get("descriptionUser");
        Property propertyUser = (Property) data.get("propertyUser");
        AttachedFile attachedFileUser = (AttachedFile) data.get("attachedFileUser");
        TagDomainAssociation tdaUser = (TagDomainAssociation) data.get("tagDomainAssociationUser");

        //add,update, delete property (simple user data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationUser, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyUser, propertyUser.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyUser, null, null, false));

        //add,update, delete property (admin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotationAdmin, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(propertyAdmin, propertyAdmin.toJsonObject()));
        expectOK(() -> propertyService.delete(propertyAdmin, null, null, false));

        //add,update, delete property (superadmin data)
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(annotation, "xxx", "value")
            .toJsonObject()));
        expectOK(() -> propertyService.update(property, property.toJsonObject()));
        expectOK(() -> propertyService.delete(property, null, null, false));

        //add,update, delete description (simple user data)
        expectOK(() -> descriptionService.update(descriptionUser, descriptionUser.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionUser, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationUser)
            .toJsonObject()));

        //add,update, delete description (admin data)
        expectOK(() -> descriptionService.update(descriptionAdmin, descriptionAdmin.toJsonObject()));
        expectOK(() -> descriptionService.delete(descriptionAdmin, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotationAdmin)
            .toJsonObject()));

        //add,update, delete description (superadmin data)
        expectOK(() -> descriptionService.update(description, description.toJsonObject()));
        expectOK(() -> descriptionService.delete(description, null, null, false));
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(annotation).toJsonObject()));

        //add,update, delete tagDomainAssociation (simple user data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationUser
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaUser, null, null, false));

        //add,update, delete tagDomainAssociation (admin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotationAdmin
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tdaAdmin, null, null, false));

        //add,update, delete tagDomainAssociation (superadmin data)
        expectOK(() -> tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            annotation
        ).toJsonObject()));
        expectOK(() -> tagDomainAssociationService.delete(tda, null, null, false));

        //add,update, delete attachedFile (simple user data)
        expectOK(() -> attachedFileService.delete(attachedFileUser, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationUser.getId(),
            annotationUser.getClass().getName()
        ));

        //add,update, delete attachedFile (admin data)
        expectOK(() -> attachedFileService.delete(attachedFileAdmin, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotationAdmin.getId(),
            annotationAdmin.getClass().getName()
        ));

        //add,update, delete attachedFile (superadmin data)
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, false));
        expectOK(() -> attachedFileService.create(
            "test.txt",
            "hello".getBytes(),
            "test",
            annotation.getId(),
            annotation.getClass().getName()
        ));

        //start reviewing image (simple user data)
        expectOK(() -> imageInstanceService.startReview(imageUser));
        expectOK(() -> imageInstanceService.stopReview(imageUser, false));

        //start reviewing image (admin data)
        expectOK(() -> imageInstanceService.startReview(imageAdmin));
        expectOK(() -> imageInstanceService.stopReview(imageAdmin, false));

        //start reviewing image (superadmin data)
        expectOK(() -> imageInstanceService.startReview(image));
        expectOK(() -> imageInstanceService.stopReview(image, false));

        //add annotation on my layer
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(slice).toJsonObject()));
        //add annotation on other layers
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceUser).toJsonObject()));
        expectOK(() -> userAnnotationService.add(builder.givenAUserAnnotation(sliceAdmin).toJsonObject()));

        //update, delete annotation (simple user data)
        expectOK(() -> userAnnotationService.update(annotationUser, annotationUser.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationUser, null, null, false));

        //update, delete annotation (admin data)
        expectOK(() -> userAnnotationService.update(annotationAdmin, annotationAdmin.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotationAdmin, null, null, false));

        //update, delete annotation (super admin data)
        expectOK(() -> userAnnotationService.update(annotation, annotation.toJsonObject()));
        expectOK(() -> userAnnotationService.delete(annotation, null, null, false));

        //add image instance
        expectOK(() -> imageInstanceService.add(builder.givenANotPersistedImageInstance(project).toJsonObject()));

        //update, delete image instance (simple user data)
        expectOK(() -> imageInstanceService.update(imageUser, imageUser.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageUser, null, null, false));

        //update, delete image instance (admin data)
        expectOK(() -> imageInstanceService.update(imageAdmin, imageAdmin.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(imageAdmin, null, null, false));

        //update, delete image instance (superadmin data)
        expectOK(() -> imageInstanceService.update(image, image.toJsonObject()));
        expectOK(() -> imageInstanceService.delete(image, null, null, false));
    }

    private Map<String, Object> initProjectDataSet(Project project) {

        Map<String, Object> result = new HashMap<>();

        //Add a simple project user
        User simpleUser = (User) userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get();

        //Add a project admin
        User admin = builder.givenAUser();
        builder.addUserToProject(project, admin.getUsername(), ADMINISTRATION);

        /*super admin data*/
        //Create an annotation (by superadmin)
        ImageInstance image = builder.givenAnImageInstance(project);
        SliceInstance slice = builder.givenASliceInstance(image, builder.givenAnAbstractSlice());
        UserAnnotation annotation = builder.givenAUserAnnotation(slice);
        //Create a description
        Description description = builder.givenADescription(annotation);
        //Create a property
        Property property = builder.givenAProperty(annotation);
        //Create an attached file
        AttachedFile attachedFile = builder.givenAnAttachedFile(annotation);
        //Create a tag
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), annotation);

        result.put("image", image);
        result.put("slice", slice);
        result.put("annotation", annotation);
        result.put("description", description);
        result.put("property", property);
        result.put("attachedFile", attachedFile);
        result.put("tagDomainAssociation", tda);

        /*admin data*/
        //Create an annotation (by admin)
        ImageInstance imageAdmin = builder.givenAnImageInstance(project);
        imageAdmin.setUser(admin);
        builder.persistAndReturn(imageAdmin);

        SliceInstance sliceAdmin = builder.givenASliceInstance(imageAdmin, builder.givenAnAbstractSlice());
        UserAnnotation annotationAdmin = builder.givenAUserAnnotation(slice);
        annotationAdmin.setUser(admin);
        builder.persistAndReturn(annotationAdmin);
        //Create a description
        Description descriptionAdmin = builder.givenADescription(annotationAdmin);
        //Create a property
        Property propertyAdmin = builder.givenAProperty(annotationAdmin);
        //Create an attached file
        AttachedFile attachedFileAdmin = builder.givenAnAttachedFile(annotationAdmin);
        //Create a tag
        TagDomainAssociation tdaAdmin = builder.givenATagAssociation(builder.givenATag(), annotationAdmin);

        result.put("imageAdmin", imageAdmin);
        result.put("sliceAdmin", sliceAdmin);
        result.put("annotationAdmin", annotationAdmin);
        result.put("descriptionAdmin", descriptionAdmin);
        result.put("propertyAdmin", propertyAdmin);
        result.put("attachedFileAdmin", attachedFileAdmin);
        result.put("tagDomainAssociationAdmin", tdaAdmin);

        /*simple user data*/
        //Create an annotation (by user)
        ImageInstance imageUser = builder.givenAnImageInstance(project);
        imageUser.setUser(simpleUser);
        builder.persistAndReturn(imageUser);

        SliceInstance sliceUser = builder.givenASliceInstance(imageUser, builder.givenAnAbstractSlice());
        UserAnnotation annotationUser = builder.givenAUserAnnotation(slice);
        annotationUser.setUser(simpleUser);
        builder.persistAndReturn(annotationUser);
        //Create a description
        Description descriptionUser = builder.givenADescription(annotationUser);
        //Create a property
        Property propertyUser = builder.givenAProperty(annotationUser);
        //Create an attached file
        AttachedFile attachedFileUser = builder.givenAnAttachedFile(annotationUser);
        //Create a tag
        TagDomainAssociation tdaUser = builder.givenATagAssociation(builder.givenATag(), annotationUser);

        result.put("imageUser", imageUser);
        result.put("sliceUser", sliceUser);
        result.put("annotationUser", annotationUser);
        result.put("descriptionUser", descriptionUser);
        result.put("propertyUser", propertyUser);
        result.put("attachedFileUser", attachedFileUser);
        result.put("tagDomainAssociationUser", tdaUser);

        return result;
    }

    // **************
    // OVERRIDE
    // **************

    @Override
    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_without_permission_add_domain() {
        expectOK(this::when_i_add_domain);
        // User with no ACL can create an project
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_permission_add_domain() {
        expectOK(this::when_i_add_domain);
        // User with READ permission can create another project
    }

    @Override
    public void when_i_get_domain() {
        projectService.get(project.getId());
    }

    @Override
    protected void when_i_add_domain() {
        projectService.add(basicInstanceBuilder.givenANotPersistedProject().toJsonObject());
    }

    @Override
    public void when_i_edit_domain() {
        projectService.update(project, project.toJsonObject());
    }

    @Override
    protected void when_i_delete_domain() {
        Project projectToDelete = project;
        projectService.delete(projectToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.empty();
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.ADMINISTRATION);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_USER");
    }
}
