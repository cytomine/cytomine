package be.cytomine.controller.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.LastConnection;
import be.cytomine.domain.social.PersistentConnection;
import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentProjectConnection;
import be.cytomine.domain.social.PersistentUserPosition;
import be.cytomine.dto.image.AreaDTO;
import be.cytomine.repositorynosql.social.LastConnectionRepository;
import be.cytomine.repositorynosql.social.LastUserPositionRepository;
import be.cytomine.repositorynosql.social.PersistentConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentImageConsultationRepository;
import be.cytomine.repositorynosql.social.PersistentProjectConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentUserPositionRepository;
import be.cytomine.repositorynosql.social.ProjectConnectionRepository;
import be.cytomine.service.PermissionService;
import be.cytomine.service.database.SequenceService;
import be.cytomine.service.social.ImageConsultationService;
import be.cytomine.service.social.ProjectConnectionService;
import be.cytomine.service.social.UserPositionService;
import be.cytomine.service.social.UserPositionServiceTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class UserResourceTests {

    @Autowired
    private PersistentConnectionRepository persistentConnectionRepository;

    @Autowired
    private LastConnectionRepository lastConnectionRepository;

    @Autowired
    private PersistentImageConsultationRepository persistentImageConsultationRepository;

    @Autowired
    private PersistentProjectConnectionRepository persistentProjectConnectionRepository;

    @Autowired
    private ProjectConnectionRepository projectConnectionRepository;

    @Autowired
    private PersistentUserPositionRepository persistentUserPositionRepository;

    @Autowired
    private LastUserPositionRepository lastUserPositionRepository;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private UserPositionService userPositionService;

    @Autowired
    private ImageConsultationService imageConsultationService;

    @Autowired
    private ProjectConnectionService projectConnectionService;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restUserControllerMockMvc;

    @Autowired
    private PermissionService permissionService;

    @BeforeEach
    public void init() {
        persistentConnectionRepository.deleteAll();
        lastConnectionRepository.deleteAll();
        persistentImageConsultationRepository.deleteAll();
        persistentProjectConnectionRepository.deleteAll();
        projectConnectionRepository.deleteAll();
        lastUserPositionRepository.deleteAll();
        persistentUserPositionRepository.deleteAll();
    }

    PersistentProjectConnection givenAPersistentConnectionInProject(
        User user, Project project,
        Date created
    ) {
        return projectConnectionService.add(user, project, "xxx", "linux", "chrome", "123", created);
    }

    PersistentImageConsultation givenAPersistentImageConsultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }


    PersistentUserPosition givenAPersistentUserPosition(
        Date creation, User user,
        SliceInstance sliceInstance,
        AreaDTO areaDTO
    ) {
        return userPositionService.add(
            creation, user, sliceInstance, sliceInstance.getImage(),
            areaDTO,
            1,
            5.0,
            false
        );
    }

    PersistentConnection givenALastConnection(User user, Long idProject, Date date) {
        LastConnection connection = new LastConnection();
        connection.setId(sequenceService.generateID());
        connection.setUser(user.getId());
        connection.setDate(date);
        connection.setCreated(date);
        connection.setProject(idProject);
        lastConnectionRepository.insert(connection); //don't use save (stateless collection)

        PersistentConnection connectionPersist = new PersistentConnection();
        connectionPersist.setId(sequenceService.generateID());
        connectionPersist.setUser(user.getId());
        connectionPersist.setCreated(date);
        connectionPersist.setProject(idProject);
        connectionPersist.setSession(RequestContextHolder.currentRequestAttributes().getSessionId());
        persistentConnectionRepository.insert(connectionPersist); //don't use save (stateless collection)
        return connectionPersist;
    }

    @Test
    @Transactional
    public void listProjectAdmin() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/project/{id}/admin.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").doesNotExist());

    }

    @Test
    @Transactional
    public void listProjectAdminAsNonAdminUser() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/project/{id}/admin.json", project.getId()).with(
                user(projectAdmin.getUsername())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").doesNotExist());

    }


    @Test
    @Transactional
    public void listProjectRepresentatives() throws Exception {
        User projectPrepresentative = builder.givenAUser();
        User projectUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectPrepresentative.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);
        builder.givenAProjectRepresentativeUser(project, projectPrepresentative);

        restUserControllerMockMvc.perform(
                get("/api/project/{id}/users/representative.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectPrepresentative.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listProjectCreator() throws Exception {
        User projectCreator = builder.givenSuperAdmin();
        User projectUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectCreator.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);
        builder.givenAProjectRepresentativeUser(project, projectCreator);

        restUserControllerMockMvc.perform(
                get("/api/project/{id}/users/representative.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectCreator.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").doesNotExist());
    }


    @Test
    @Transactional
    public void listOntologyUser() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Ontology ontology = builder.givenAnOntology();
        Project project = builder.givenAProjectWithOntology(ontology);
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/ontology/{id}/user.json", ontology.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());
    }


    @Test
    @Transactional
    public void listStorageUser() throws Exception {
        User storageAdmin = builder.givenAUser();
        User storageUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Storage storage = builder.givenAStorage();
        builder.addUserToStorage(storage, storageAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToStorage(storage, storageUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/storage/{id}/user.json", storage.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + storageAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + storageUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listProjectUserlayer() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/project/{id}/userlayer.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listUser() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/user.json")
                .param("sortColumn", "created")
                .param("sortDirection", "desc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')].name")
                .value("firstname lastname"))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')].name")
                .value("firstname lastname"))
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')].name")
                .value("firstname lastname"));
    }

    @Test
    @Transactional
    public void getUserAsSuperadmin() throws Exception {
        User currentUser = builder.givenSuperAdmin();

        restUserControllerMockMvc.perform(get("/api/user/{id}.json", builder.givenSuperAdmin().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(currentUser.getId()))
            .andExpect(jsonPath("$.username").value(currentUser.getUsername()))
            .andExpect(jsonPath("$.name").value(currentUser.getName()))
            .andExpect(jsonPath("$.fullName").value(currentUser.getFullName()))
            .andExpect(jsonPath("$.firstname").doesNotExist())
            .andExpect(jsonPath("$.origin").doesNotExist())
            .andExpect(jsonPath("$.admin").doesNotExist())
            .andExpect(jsonPath("$.publicKey").doesNotExist())
            .andExpect(jsonPath("$.lastname").doesNotExist())
            .andExpect(jsonPath("$.privateKey").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.guest").doesNotExist())
            .andExpect(jsonPath("$.passwordExpired").doesNotExist())
            .andExpect(jsonPath("$.user").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void getUserAsCurrentUser() throws Exception {
        User currentUser = builder.givenDefaultUser();

        restUserControllerMockMvc.perform(get("/api/user/{id}.json", currentUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(currentUser.getId()))
            .andExpect(jsonPath("$.username").value(currentUser.getUsername()))
            .andExpect(jsonPath("$.name").value(currentUser.getName()))
            .andExpect(jsonPath("$.fullName").value(currentUser.getFullName()))
            .andExpect(jsonPath("$.firstname").doesNotExist())
            .andExpect(jsonPath("$.origin").doesNotExist())
            .andExpect(jsonPath("$.admin").doesNotExist())
            .andExpect(jsonPath("$.publicKey").doesNotExist())
            .andExpect(jsonPath("$.lastname").doesNotExist())
            .andExpect(jsonPath("$.privateKey").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.guest").doesNotExist())
            .andExpect(jsonPath("$.passwordExpired").doesNotExist())
            .andExpect(jsonPath("$.user").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void getUserAsAnotherUser() throws Exception {
        User currentUser = builder.givenAUser();

        restUserControllerMockMvc.perform(get("/api/user/{id}.json", builder.givenSuperAdmin().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(builder.givenSuperAdmin().getId()))
            .andExpect(jsonPath("$.username").value(builder.givenSuperAdmin().getUsername()))
            .andExpect(jsonPath("$.name").value(builder.givenSuperAdmin().getName()))
            .andExpect(jsonPath("$.fullName").value(builder.givenSuperAdmin().getFullName()))
            .andExpect(jsonPath("$.firstname").doesNotExist())
            .andExpect(jsonPath("$.origin").doesNotExist())
            .andExpect(jsonPath("$.admin").doesNotExist())
            .andExpect(jsonPath("$.publicKey").doesNotExist())
            .andExpect(jsonPath("$.lastname").doesNotExist())
            .andExpect(jsonPath("$.privateKey").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.guest").doesNotExist())
            .andExpect(jsonPath("$.passwordExpired").doesNotExist())
            .andExpect(jsonPath("$.user").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @Transactional
    public void getUserWithItsUsername() throws Exception {
        User currentUser = builder.givenSuperAdmin();
        restUserControllerMockMvc.perform(get("/api/user/{id}.json", builder.givenSuperAdmin().getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(currentUser.getUsername()));
    }

    @Test
    @Transactional
    public void getUserWithItsUserKey() throws Exception {
        User currentUser = builder.givenSuperAdmin();
        restUserControllerMockMvc.perform(get(
                "/api/userkey/{publicKey}/keys.json",
                builder.givenSuperAdmin().getPublicKey()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.privateKey").value(currentUser.getPrivateKey()));
    }

    @Test
    @Transactional
    public void getUserWithItsUserKeyId() throws Exception {
        User currentUser = builder.givenSuperAdmin();
        restUserControllerMockMvc.perform(get("/api/user/{id}/keys.json", builder.givenSuperAdmin().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.privateKey").value(currentUser.getPrivateKey()));
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void getKeysFromOtherUserIsForbidden() throws Exception {
        User user = builder.givenSuperAdmin();
        restUserControllerMockMvc.perform(get("/api/user/{id}/keys.json", user.getId()))
            .andExpect(status().isForbidden());
        restUserControllerMockMvc.perform(get("/api/userkey/{publicKey}/keys.json", user.getPublicKey()))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void getSignature() throws Exception {
        User user = builder.givenDefaultUser();
        restUserControllerMockMvc.perform(get("/api/signature.json").param("method", "GET"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicKey").value(user.getPublicKey()))
            .andExpect(jsonPath("$.signature").isNotEmpty());
    }

    @Test
    @Transactional
    public void getCurrentUserKeys() throws Exception {
        User currentUser = builder.givenSuperAdmin();

        restUserControllerMockMvc.perform(get("/api/user/current/keys"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.primaryKey").value(currentUser.getPublicKey()))
            .andExpect(jsonPath("$.secondaryKey").value(currentUser.getPrivateKey()));
    }

    @Test
    @Transactional
    public void regenerateCurrentUserKeys() throws Exception {
        User currentUser = builder.givenSuperAdmin();

        String oldPrimaryKey = currentUser.getPublicKey();
        String oldSecondaryKey = currentUser.getPrivateKey();

        restUserControllerMockMvc.perform(post("/api/user/current/keys"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.primaryKey").value(not(oldPrimaryKey)))
            .andExpect(jsonPath("$.secondaryKey").value(not(oldSecondaryKey)));
    }


    @Test
    @Transactional
    public void getCurrentUser() throws Exception {
        User currentUser = builder.givenSuperAdmin();

        restUserControllerMockMvc.perform(get("/api/user/current.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(currentUser.getId()))
            .andExpect(jsonPath("$.username").value(currentUser.getUsername()))
            .andExpect(jsonPath("$.name").value(currentUser.getName()))
            .andExpect(jsonPath("$.fullName").value(currentUser.getFullName()))
            .andExpect(jsonPath("$.firstname").doesNotExist())
            .andExpect(jsonPath("$.origin").doesNotExist())
            .andExpect(jsonPath("$.admin").doesNotExist())
            .andExpect(jsonPath("$.publicKey").doesNotExist())
            .andExpect(jsonPath("$.lastname").doesNotExist())
            .andExpect(jsonPath("$.privateKey").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.guest").doesNotExist())
            .andExpect(jsonPath("$.passwordExpired").doesNotExist())
            .andExpect(jsonPath("$.user").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    //    @Test
    //    @Transactional
    //    public void addValidUser() throws Exception {
    //
    //        restUserControllerMockMvc.perform(post("/api/user.json")
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content("{\"name\":\"TEST_CREATE\",\"reference\":\""+ UUID.randomUUID
    //                        ().toString() +"\",\"firstname\":\"TEST_CREATE\",
    //                        \"lastname\":\"TEST_CREATE\",\"username\":\"TEST_CREATE\",
    //                        \"email\":\"loicrollus@gmail.com\",\"language\":\"EN\",
    //                        \"password\":\"TEST_CREATE\"}"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$.printMessage").value(true))
    //                .andExpect(jsonPath("$.callback").exists())
    //                .andExpect(jsonPath("$.callback.userID").exists())
    //                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddUserCommand"))
    //                .andExpect(jsonPath("$.message").exists())
    //                .andExpect(jsonPath("$.command").exists())
    //                .andExpect(jsonPath("$.user.id").exists())
    //                .andExpect(jsonPath("$.user.username").value("TEST_CREATE"));
    //
    //        User user = userRepository.findByUsernameLikeIgnoreCase("TEST_CREATE").get();
    //
    //    }
    //
    //
    //    @Test
    //    @Transactional
    //    public void addUserRefusedIfUsernameExists() throws Exception {
    //        User user = builder.given_a_user();
    //        restUserControllerMockMvc.perform(post("/api/user.json")
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content(user.toJSON()))
    //                .andDo(print())
    //                .andExpect(status().isConflict())
    //                .andExpect(jsonPath("$.success").value(false));
    //    }
    //
    //    @Test
    //    @Transactional
    //    public void addUserRefusedIfUsernameNotSet() throws Exception {
    //        User user = builder.given_a_user();
    //        restUserControllerMockMvc.perform(post("/api/user.json")
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content("{\"firstname\":\"TEST_CREATE\",\"lastname\":\"TEST_CREATE\",
    //                        \"email\":\"loicrollus@gmail.com\",\"language\":\"EN\",
    //                        \"password\":\"TEST_CREATE\"}"))
    //                .andDo(print())
    //                .andExpect(status().isBadRequest())
    //                .andExpect(jsonPath("$.success").value(false));
    //    }
    //
    //    @Test
    //    @Transactional
    //    public void editValidUser() throws Exception {
    //
    //        restUserControllerMockMvc.perform(post("/api/user.json")
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content("{\"name\":\"TEST_CREATE\",\"reference\":\""+ UUID.randomUUID
    //                        ().toString() +"\",\"firstname\":\"TEST_CREATE\",
    //                        \"lastname\":\"TEST_CREATE\",\"username\":\"TEST_CREATE\",
    //                        \"email\":\"loicrollus@gmail.com\",\"language\":\"EN\",
    //                        \"password\":\"TEST_CREATE\"}"))
    //                .andDo(print())
    //                .andExpect(status().isOk());
    //
    //
    //        User user = userRepository.findByUsernameLikeIgnoreCase("TEST_CREATE").get();
    //
    //        JsonObject jsonObject = user.toJsonObject();
    //        jsonObject.put("name", "TEST_CREATE_CHANGE");
    //
    //        restUserControllerMockMvc.perform(put("/api/user/{id}.json", jsonObject.getId())
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content(jsonObject.toJsonString()))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$.printMessage").value(true))
    //                .andExpect(jsonPath("$.callback").exists())
    //                .andExpect(jsonPath("$.message").exists())
    //                .andExpect(jsonPath("$.command").exists())
    //                .andExpect(jsonPath("$.user.id").exists())
    //                .andExpect(jsonPath("$.user.name").value("TEST_CREATE_CHANGE"));
    //    }
    //
    //    @Test
    //    @Transactional
    //    public void deleteUser() throws Exception {
    //        User user = builder.given_a_user();
    //        restUserControllerMockMvc.perform(delete("/api/user/{id}.json", user.getId())
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content(user.toJSON()))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$.printMessage").value(true))
    //                .andExpect(jsonPath("$.callback").exists())
    //                .andExpect(jsonPath("$.callback.userID").exists())
    //                .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteUserCommand"))
    //                .andExpect(jsonPath("$.message").exists())
    //                .andExpect(jsonPath("$.command").exists())
    //                .andExpect(jsonPath("$.user.id").exists());
    //    }

    @Test
    @Transactional
    public void listProjectUsersWithRoleFilter() throws Exception {
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User projectPrepresentative = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Project project = builder.givenAProject();

        builder.addUserToProject(project, projectPrepresentative.getUsername(), ADMINISTRATION);
        builder.givenAProjectRepresentativeUser(project, projectPrepresentative);
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/project/{id}/user.json", project.getId())
                .param("max", "25")
                .param("offset", "0")
                .param("projectRole[in]", "contributor,manager,representative")
                .param("sort", "")
                .param("order", "")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectPrepresentative.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());

        restUserControllerMockMvc.perform(get("/api/project/{id}/user.json", project.getId())
                .param("max", "25")
                .param("offset", "0")
                .param("projectRole[in]", "representative")
                .param("sort", "")
                .param("order", "")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectPrepresentative.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").doesNotExist())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").doesNotExist())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listProjectUsersWithPagination() throws Exception {
        User projectPrepresentative = builder.givenAUser();
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Project project = builder.givenAProject();

        builder.addUserToProject(project, projectPrepresentative.getUsername(), ADMINISTRATION);
        builder.givenAProjectRepresentativeUser(project, projectPrepresentative);
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(get("/api/project/{id}/user.json", project.getId())
                .param("max", "2")
                .param("offset", "0")
                .param("projectRole[in]", "contributor,manager,representative")
                .param("sortColumn", "created")
                .param("sortDirection", "asc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(2)))
            .andExpect(jsonPath("$.offset").value(0))
            .andExpect(jsonPath("$.perPage").value(2))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.collection[0].username").value(projectPrepresentative.getUsername()))
            .andExpect(jsonPath("$.collection[1].username").value(projectAdmin.getUsername()));

        restUserControllerMockMvc.perform(get("/api/project/{id}/user.json", project.getId())
                .param("max", "2")
                .param("offset", "2")
                .param("projectRole[in]", "contributor,manager,representative")
                .param("sortColumn", "created")
                .param("sortDirection", "asc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1)))
            .andExpect(jsonPath("$.offset").value(2))
            .andExpect(jsonPath("$.perPage").value(1))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.collection[0].username").value(projectUser.getUsername()));

        restUserControllerMockMvc.perform(get("/api/project/{id}/user.json", project.getId())
                .param("max", "2")
                .param("offset", "4")
                .param("projectRole[in]", "contributor,manager,representative")
                .param("sortColumn", "created")
                .param("sortDirection", "asc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(0)))
            .andExpect(jsonPath("$.offset").value(4))
            .andExpect(jsonPath("$.perPage").value(0))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @Transactional
    public void addUserToProject() throws Exception {

        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        restUserControllerMockMvc.perform(
                post("/api/project/{project}/user/{user}.json", project.getId(), user.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void addUsersToProject() throws Exception {
        Project project = builder.givenAProject();
        User user1 = builder.givenAUser();
        User user2 = builder.givenAUser();
        restUserControllerMockMvc.perform(post("/api/project/{project}/user.json", project.getId())
                .param("users", user1.getId() + "," + user2.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user2.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user2.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void shouldPartiallyAddUsersToProjectWhenSomeUserIdsAreInvalid() throws Exception {
        Project project = builder.givenAProject();
        User user1 = builder.givenAUser();
        restUserControllerMockMvc.perform(post("/api/project/{project}/user.json", project.getId())
                .param("users", user1.getId() + ",xxxxxx,0") //bad format + bad id
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isPartialContent());

        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), ADMINISTRATION)).isFalse();
    }


    @Test
    @Transactional
    public void deleteUserFromProject() throws Exception {

        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        builder.addUserToProject(project, user.getUsername(), READ);
        restUserControllerMockMvc.perform(
                delete("/api/project/{project}/user/{user}.json", project.getId(), user.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void deleteUsersFromProject() throws Exception {
        Project project = builder.givenAProject();
        User user1 = builder.givenAUser();
        User user2 = builder.givenAUser();
        builder.addUserToProject(project, user1.getUsername(), READ);
        builder.addUserToProject(project, user2.getUsername(), READ);
        restUserControllerMockMvc.perform(
                delete("/api/project/{project}/user.json", project.getId())
                    .param("users", user1.getId() + "," + user2.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user2.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user2.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void shouldPartiallyRemoveUsersFromProjectWhenSomeUserIdsAreInvalid() throws Exception {
        Project project = builder.givenAProject();
        User user1 = builder.givenAUser();
        restUserControllerMockMvc.perform(
                delete("/api/project/{project}/user.json", project.getId())
                    .param("users", user1.getId() + ",xxxxxx,0") //bad format + bad id
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isPartialContent());

        assertThat(permissionService.hasACLPermission(project, user1.getUsername(), READ)).isFalse();
    }

    @Test
    @Transactional
    public void addAdminToProject() throws Exception {

        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        restUserControllerMockMvc.perform(
                post("/api/project/{project}/user/{user}/admin.json", project.getId(), user.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isTrue();
    }

    @Test
    @Transactional
    public void deleteAdminFromProject() throws Exception {
        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        builder.addUserToProject(project, user.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, user.getUsername(), READ);
        restUserControllerMockMvc.perform(
                delete(
                    "/api/project/{project}/user/{user}/admin.json", project.getId(),
                    user.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void addUserToStorage() throws Exception {
        Storage storage = builder.givenAStorage();
        User user = builder.givenAUser();
        restUserControllerMockMvc.perform(
                post("/api/storage/{storage}/user/{user}.json", storage.getId(), user.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void deleteUserFromStorage() throws Exception {
        Storage storage = builder.givenAStorage();
        User user = builder.givenAUser();
        builder.addUserToStorage(storage, user.getUsername(), READ);
        restUserControllerMockMvc.perform(
                delete("/api/storage/{storage}/user/{user}.json", storage.getId(), user.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), ADMINISTRATION)).isFalse();
    }

    @Test
    @Transactional
    public void listFriends() throws Exception {
        User projectPrepresentative = builder.givenAUser();
        User projectAdmin = builder.givenAUser();
        User projectUser = builder.givenAUser();
        User simpleUser = builder.givenAUser();
        Project project = builder.givenAProject();

        builder.addUserToProject(project, projectPrepresentative.getUsername(), ADMINISTRATION);
        builder.givenAProjectRepresentativeUser(project, projectPrepresentative);
        builder.addUserToProject(project, projectAdmin.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, projectUser.getUsername(), READ);

        restUserControllerMockMvc.perform(
                get("/api/user/{id}/friends.json", projectPrepresentative.getId())
                    .param("project", project.getId().toString())
                    .param("offline", "true")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectPrepresentative.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());

        restUserControllerMockMvc.perform(
                get("/api/user/{id}/friends.json", projectPrepresentative.getId())
                    .param("project", project.getId().toString())
                    .param("offline", "false")
            )
            .andExpect(status().isOk());

        restUserControllerMockMvc.perform(
                get("/api/user/{id}/friends.json", projectPrepresentative.getId())
                    .param("offline", "false")
            )
            .andExpect(status().isOk());

        restUserControllerMockMvc.perform(
                get("/api/user/{id}/friends.json", projectPrepresentative.getId())
                    .param("offline", "true")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[?(@.username=='"
                + projectPrepresentative.getUsername()
                + "')]").doesNotExist())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectAdmin.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + projectUser.getUsername() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.username=='" + simpleUser.getUsername() + "')]").doesNotExist());
    }

    @Test
    @Transactional
    public void listOnlineUsers() throws Exception {
        User userOnline = builder.givenDefaultUser();
        User userOnlineButOnDifferentProject = builder.givenAUser();
        User userOffline = builder.givenAUser();

        Project project = builder.givenAProject();
        Project anotherProject = builder.givenAProject();

        givenALastConnection(userOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(userOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));
        givenALastConnection(
            userOnlineButOnDifferentProject, anotherProject.getId(),
            DateUtils.addSeconds(new Date(), -10)
        );

        PersistentUserPosition persistentUserPosition =
            givenAPersistentUserPosition(
                DateUtils.addSeconds(new Date(), -15),
                userOnline,
                builder.givenANotPersistedSliceInstance(
                    builder.givenAnImageInstance(project),
                    builder.givenAnAbstractSlice()
                ),
                UserPositionServiceTests.USER_VIEW
            );

        restUserControllerMockMvc.perform(get("/api/project/{id}/online/user.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1)))
            .andExpect(jsonPath("$.collection[0].id").value(userOnline.getId()))
            .andExpect(jsonPath("$.collection[0].position").hasJsonPath())
            .andExpect(jsonPath("$.collection[0].position[0].image").value(persistentUserPosition.getImage()))
            .andExpect(jsonPath("$.collection[0].position[0].filename").hasJsonPath())
            .andExpect(jsonPath("$.collection[0].position[0].originalFilename").hasJsonPath())
            .andExpect(jsonPath("$.collection[0].position[0].date")
                .value(persistentUserPosition.getCreated().getTime()));
    }

    @Test
    @Transactional
    public void listUserActivity() throws Exception {
        User userOnline = builder.givenDefaultUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userOnline.getUsername());

        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        PersistentImageConsultation consultation = givenAPersistentImageConsultation(
            userOnline,
            builder.givenAnImageInstance(project),
            new Date()
        );

        restUserControllerMockMvc.perform(get("/api/project/{id}/usersActivity.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1)))
            .andExpect(jsonPath("$.collection[0].id").value(userOnline.getId()))
            .andExpect(jsonPath("$.collection[0].username").value(userOnline.getUsername()))
            .andExpect(jsonPath("$.collection[0].name").value(userOnline.getName()))
            .andExpect(jsonPath("$.collection[0].fullName").value(userOnline.getFullName()))
            .andExpect(jsonPath("$.collection[0].lastImageId").value(consultation.getImage()))
            .andExpect(jsonPath("$.collection[0].lastImageName").hasJsonPath())
            .andExpect(jsonPath("$.collection[0].lastConnection").value(lastConnection.getCreated().getTime()))
            .andExpect(jsonPath("$.collection[0].frequency").value(1));
    }

    @Test
    void getResumeActivity() throws Exception {
        User userOnline = builder.givenDefaultUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, userOnline.getUsername());

        PersistentProjectConnection firstConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addDays(new Date(), -15)
        );
        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        givenAPersistentImageConsultation(userOnline, builder.givenAnImageInstance(project), new Date());

        restUserControllerMockMvc.perform(
                get("/api/project/{id}/resumeActivity/{user}.json", project.getId(), userOnline.getId())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstConnection").value(firstConnection.getCreated().getTime()))
            .andExpect(jsonPath("$.lastConnection").value(lastConnection.getCreated().getTime()))
            .andExpect(jsonPath("$.totalAnnotations").value(0))
            .andExpect(jsonPath("$.totalConnections").value(2))
            .andExpect(jsonPath("$.totalConsultations").value(1))
            .andExpect(jsonPath("$.totalAnnotationSelections").value(0));
    }

    @Test
    @Transactional
    public void downloadUserListFromProjectXlsDocument() throws Exception {
        User user = builder.givenAUser("Paul");
        Project project = builder.givenAProjectWithUser(user);
        MvcResult mvcResult = performDownload("xls", project, "application/octet-stream");
        checkXLSResult(mvcResult, user);
    }

    @Test
    @Transactional
    public void downloadUserListFromProjectCsvDocument() throws Exception {
        User user = builder.givenAUser("Paul");
        Project project = builder.givenAProjectWithUser(user);
        MvcResult mvcResult = performDownload("csv", project, "text/csv");
        checkResult(";", mvcResult, user);
    }

    @Test
    @Transactional
    public void downloadUserListFromProjectPdfDocument() throws Exception {
        Project project = builder.givenAProjectWithUser(builder.givenAUser());
        performDownload("pdf", project, "application/pdf");
    }

    private void checkResult(String delimiter, MvcResult result, User user)
        throws UnsupportedEncodingException {
        String[] rows = result.getResponse().getContentAsString().split("\r\n|\r|\n");
        String[] userAnnotationResult = rows[1].split(delimiter);
        AssertionsForClassTypes.assertThat(userAnnotationResult[0]).isEqualTo(user.getUsername());
        AssertionsForClassTypes.assertThat(userAnnotationResult[1]).isEqualTo(user.getName());
    }

    private void checkXLSResult(MvcResult result, User user) throws IOException {
        byte[] spreadsheetData = result.getResponse().getContentAsByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(spreadsheetData);
        Workbook workbook = null;

        workbook = new HSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(1); // Assuming the data starts from the second row
        Cell[] cells = new Cell[row.getLastCellNum()];
        for (int i = 0; i < row.getLastCellNum(); i++) {
            cells[i] = row.getCell(i);
        }

        AssertionsForClassTypes.assertThat(cells[0].getStringCellValue()).isEqualTo(user.getUsername());
        AssertionsForClassTypes.assertThat(cells[1].getStringCellValue()).isEqualTo(user.getName());

        workbook.close();
    }

    private MvcResult performDownload(String format, Project project, String type) throws Exception {
        return restUserControllerMockMvc.perform(
                get("/api/project/{project}/user/download", project.getId()).param("format", format)
            )
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", type))
            .andReturn();
    }
}
