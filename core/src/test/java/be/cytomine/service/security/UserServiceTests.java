package be.cytomine.service.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.config.MockedUser;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.LastConnection;
import be.cytomine.domain.social.PersistentConnection;
import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentProjectConnection;
import be.cytomine.domain.social.PersistentUserPosition;
import be.cytomine.dto.auth.AuthInformation;
import be.cytomine.dto.image.AreaDTO;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.repositorynosql.social.LastConnectionRepository;
import be.cytomine.repositorynosql.social.LastUserPositionRepository;
import be.cytomine.repositorynosql.social.PersistentConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentImageConsultationRepository;
import be.cytomine.repositorynosql.social.PersistentProjectConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentUserPositionRepository;
import be.cytomine.repositorynosql.social.ProjectConnectionRepository;
import be.cytomine.service.PermissionService;
import be.cytomine.service.database.SequenceService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.search.UserSearchExtension;
import be.cytomine.service.social.ImageConsultationService;
import be.cytomine.service.social.ProjectConnectionService;
import be.cytomine.service.social.UserPositionService;
import be.cytomine.service.social.UserPositionServiceTests;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.filters.SearchOperation;
import be.cytomine.utils.filters.SearchParameterEntry;

import static be.cytomine.BasicInstanceBuilder.ACL_USER_NO_ACL;
import static be.cytomine.authorization.AbstractAuthorizationTest.CREATOR;
import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static be.cytomine.authorization.AbstractAuthorizationTest.USER_ACL_ADMIN;
import static be.cytomine.authorization.AbstractAuthorizationTest.USER_ACL_CREATE;
import static be.cytomine.authorization.AbstractAuthorizationTest.USER_ACL_DELETE;
import static be.cytomine.authorization.AbstractAuthorizationTest.USER_ACL_READ;
import static be.cytomine.authorization.AbstractAuthorizationTest.USER_ACL_WRITE;
import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = SUPERADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@MockedUser
@Transactional
public class UserServiceTests {

    private static final WireMockServer wireMockServer = WiremockRepository.SERVER;
    @Autowired
    UserService userService;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private BasicInstanceBuilder builder;
    @Autowired
    private ImageConsultationService imageConsultationService;
    @Autowired
    private ProjectConnectionService projectConnectionService;
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
    private PermissionService permissionService;
    @Autowired
    private UserPositionService userPositionService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;

    private static void setupStub() {
        /* Simulate call to CBIR */
        wireMockServer.stubFor(delete(urlPathMatching(CBIR_API_BASE_PATH + "/images/.*"))
            .withQueryParam("storage", WireMock.matching(".*"))
            .withQueryParam("index", WireMock.equalTo("annotation"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(UUID.randomUUID().toString())
            )
        );
    }

    @BeforeAll
    public static void beforeAll() {
        WireMock.configureFor("localhost", wireMockServer.port());

        setupStub();
    }

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

    PersistentProjectConnection givenAPersistentConnectionInProject(User user, Project project, Date created) {
        return projectConnectionService.add(
            user.getId(),
            project,
            "xxx",
            "linux",
            "chrome",
            "123",
            created
        );
    }

    PersistentImageConsultation givenAPersistentImageConsultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        return imageConsultationService.add(user.getId(), imageInstance.getId(), "xxx", "mode", created);
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
    void findUnexistingUserReturnEmpty() {
        assertThat(userService.find(0L)).isEmpty();
    }

    @Test
    void findUserWithSuccess() {
        UserResponse user = builder.givenUserAclRead();
        User expected = builder.getUserEntity(user.username());
        assertThat(userService.findUser(user.id())).isPresent().contains(expected);
    }

    @Test
    void findUserByUsername() {
        UserResponse user = builder.givenUserAclRead();
        User expected = builder.getUserEntity(user.username());
        assertThat(userService.findByUsername(user.username())).isPresent().contains(expected);
        assertThat(userService.findByUsername(user.username().toUpperCase(Locale.ROOT)))
            .isPresent().contains(expected);
        assertThat(userService.findByUsername(user.username().toLowerCase(Locale.ROOT)))
            .isPresent().contains(expected);
    }

    @Test
    void findUserByPublicKey() {
        UserResponse user = builder.givenUserAclRead();
        UserResponse expected = builder.getUser(user.username());
        assertThat(userService.findByPublicKey(user.publicKey().orElseThrow())).isPresent().contains(expected);
    }

    @Test
    void getAuthRolesForUser() {
        UserResponse user = builder.givenUserAclRead();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isFalse();
        assertThat(authInformation.getUser()).isTrue();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isTrue();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }

    @Test
    void getAuthRolesForGuest() {
        UserResponse user = builder.givenGuestAcl();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isFalse();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isTrue();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isFalse();
        assertThat(authInformation.getGuestByNow()).isTrue();
    }

    @Test
    void getAuthRolesForSuperamdin() {
        UserResponse user = builder.givenSuperAdmin();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isTrue();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isTrue();
        assertThat(authInformation.getUserByNow()).isFalse();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }

    @Test
    void getAuthRolesForAdmin() {
        UserResponse user = builder.givenAdmin();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isTrue();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isTrue();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }

    @Test
    void listUsersWithNoFiltersNoExtension() {
        Page<Map<String, Object>> list = userService.list(new ArrayList<>(), "created", "desc", 0L, 0L);

        assertThat(list.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().id());
    }

    @Test
    void listUsersWithWithMultisearchFilters() {
        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry("fullName", SearchOperation.like, "superad"))),
            "created",
            "desc",
            0L,
            0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().id());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                builder.givenSuperAdmin().name().orElse(null)
            ))), "created", "desc", 0L, 0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().id());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry("fullName", SearchOperation.like, "johndoe@example.com"))),
            "created",
            "desc",
            0L,
            0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).doesNotContain(builder.givenSuperAdmin().id());
    }

    @Test
    void listUsersWithSortUsername() {
        User user1 = builder.getUserEntity(CREATOR);
        User user2 = builder.getUserEntity(USER_ACL_CREATE);

        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "creat"
            ))), "username", "asc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(2);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "creat"
            ))), "username", "desc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(2);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user1.getUsername());
    }

    @Test
    void listUsersWithPage() {
        User user1 = builder.getUserEntity(USER_ACL_ADMIN);
        User user2 = builder.getUserEntity(USER_ACL_CREATE);
        User user3 = builder.getUserEntity(USER_ACL_DELETE);
        User user4 = builder.getUserEntity(USER_ACL_READ);
        User user5 = builder.getUserEntity(USER_ACL_WRITE);

        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "user_acl_"
            ))), "username", "asc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(5);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user3.getUsername());
        assertThat(list.getContent().get(3).get("username")).isEqualTo(user4.getUsername());
        assertThat(list.getContent().get(4).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "user_acl_"
            ))), "username", "asc", 3L, 0L
        );
        assertThat(list.getContent()).hasSize(3);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user3.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "user_acl_"
            ))), "username", "asc", 4L, 2L
        );
        assertThat(list.getContent()).hasSize(3);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user3.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user4.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "user_acl_"
            ))), "username", "asc", 4L, 4L
        );
        assertThat(list.getContent()).hasSize(1);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "user_acl_"
            ))), "username", "asc", 5L, 6L
        );
        assertThat(list.getContent()).hasSize(0);
        assertThat(list.getTotalElements()).isEqualTo(5);
    }

    @Test
    void listUserByProjectWithSuccess() {
        UserResponse user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.getUserEntity(USER_ACL_READ);
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        Page<JsonObject> page = userService.listUsersByProject(
            projectWhereUserIsManager,
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("manager");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersByProject(projectWhereUserIsContributor, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersByProject(projectWhereUserIsMissing, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).doesNotContain(user.id());

        page = userService.listUsersByProject(projectWithTwoUsers, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
    }

    @Test
    void listUserExtendedWithEmptyExtension() {
        UserResponse user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.getUserEntity(USER_ACL_READ);
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        List<SearchParameterEntry> searchParameterEntries = new ArrayList<>();

        Page<JsonObject> page = userService.listUsersExtendedByProject(
            projectWhereUserIsManager,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("manager");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersExtendedByProject(
            projectWhereUserIsContributor,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersExtendedByProject(
            projectWhereUserIsMissing,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).doesNotContain(user.id());

        page = userService.listUsersExtendedByProject(
            projectWithTwoUsers,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.id());
    }

    @Test
    void listUserExtendedWithLastImageName() {
        User userWhoHasOpenImage = builder.getUserEntity(USER_ACL_READ);
        User userWhoHasOpenImageAfter = builder.getUserEntity(USER_ACL_WRITE);
        User userNeverOpenImage = builder.getUserEntity(USER_ACL_CREATE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenImage.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenImageAfter.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenImage.getUsername(), WRITE);

        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.setInstanceFilename(UUID.randomUUID().toString());

        givenAPersistentImageConsultation(userNeverOpenImage, imageInstance, DateUtils.addDays(new Date(), -2));
        givenAPersistentImageConsultation(
            userWhoHasOpenImageAfter,
            imageInstance,
            DateUtils.addDays(new Date(), -1)
        );

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithLastImage(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "lastImageName",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().stream().map(x -> x.getJSONAttrLong("id"))).contains(
            userWhoHasOpenImage.getId(),
            userWhoHasOpenImageAfter.getId()
        );
        assertThat(page.getContent().stream().map(x -> x.getJSONAttrLong("lastImage"))).contains(imageInstance.getId());
    }

    @Test
    void listUserExtendedWithLastConnection() {
        User userWhoHasOpenProject = builder.getUserEntity(USER_ACL_READ);
        User userWhoHasOpenProjectAfter = builder.getUserEntity(USER_ACL_WRITE);
        User userNeverOpenProject = builder.getUserEntity(USER_ACL_CREATE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenProject.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenProjectAfter.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenProject.getUsername(), WRITE);

        PersistentProjectConnection userWhoHasOpenProjectConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenProject,
            project,
            DateUtils.addDays(new Date(), -2)
        );
        PersistentProjectConnection userWhoHasOpenProjectAfterConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenProjectAfter,
            project,
            DateUtils.addDays(new Date(), -1)
        );

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithLastConnection(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project, userSearchExtension, new ArrayList<>(), "lastConnection", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProjectAfter.getId());
        assertThat(page.getContent().get(0).getJSONAttrDate("lastConnection")).isEqualTo(
            userWhoHasOpenProjectAfterConnection.getCreated());
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject.getId());
        assertThat(page.getContent()
            .get(1)
            .getJSONAttrDate("lastConnection")).isEqualTo(userWhoHasOpenProjectConnection.getCreated());
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(2).getJSONAttrStr("lastImage")).isNull();

        page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "lastConnection",
            "asc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(0).getJSONAttrDate("lastConnection")).isNull();
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject.getId());
        assertThat(page.getContent()
            .get(1)
            .getJSONAttrDate("lastConnection")).isEqualTo(userWhoHasOpenProjectConnection.getCreated());
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProjectAfter.getId());
        assertThat(page.getContent().get(2).getJSONAttrDate("lastConnection")).isEqualTo(
            userWhoHasOpenProjectAfterConnection.getCreated());
    }

    @Test
    void listUserExtendedWithConnectionFrequency() {
        User userWhoHasOpenOnce = builder.getUserEntity(USER_ACL_READ);
        User userWhoHasOpenProject11x = builder.getUserEntity(USER_ACL_WRITE);
        User userNeverOpenProject = builder.getUserEntity(USER_ACL_CREATE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenOnce.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenProject11x.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenProject.getUsername(), WRITE);

        PersistentProjectConnection userWhoHasOpenProjectConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenOnce,
            project,
            DateUtils.addDays(new Date(), -2)
        );
        for (int i = 0; i < 11; i++) {
            givenAPersistentConnectionInProject(
                userWhoHasOpenProject11x,
                project,
                DateUtils.addDays(new Date(), -1)
            );
        }

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithNumberConnections(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "frequency",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject11x.getId());
        assertThat(page.getContent().get(0).getJSONAttrInteger("numberConnections")).isEqualTo(11);
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenOnce.getId());
        assertThat(page.getContent().get(1).getJSONAttrInteger("numberConnections")).isEqualTo(1);
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(2).getJSONAttrInteger("numberConnections")).isEqualTo(0);

        page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "frequency",
            "asc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(0).getJSONAttrInteger("numberConnections")).isEqualTo(0);
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenOnce.getId());
        assertThat(page.getContent().get(1).getJSONAttrInteger("numberConnections")).isEqualTo(1);
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject11x.getId());
        assertThat(page.getContent().get(2).getJSONAttrInteger("numberConnections")).isEqualTo(11);
    }

    @Test
    void listProjectAdmins() {
        UserResponse user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        UserResponse anotherUser = builder.givenUserAclRead();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.username(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.username(), WRITE);

        assertThat(userService.listAdmins(projectWhereUserIsManager)).contains(user)
            .doesNotContain(anotherUser);
        assertThat(userService.listAdmins(projectWhereUserIsContributor)).doesNotContain(user);
    }

    @Test
    void listProjectUsers() {
        UserResponse user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        UserResponse anotherUser = builder.givenUserAclRead();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.username(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.username(), WRITE);

        assertThat(userService.listUsers(projectWhereUserIsManager)).contains(user)
            .doesNotContain(anotherUser);
        assertThat(userService.listUsers(projectWhereUserIsContributor)).contains(user)
            .doesNotContain(anotherUser);
        assertThat(userService.listUsers(projectWithTwoUsers)).contains(user, anotherUser);
    }

    @Test
    void findProjectCreator() {
        UserResponse user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);

        assertThat(userService.findCreator(projectWhereUserIsManager)).contains(builder.getUserEntity(user.username()));
    }

    @Test
    void listStorageUsers() {
        Storage storage = builder.givenAStorage(builder.givenSuperAdmin());

        assertThat(userService.listUsers(storage)).contains(
            builder.getUserEntity(builder.givenSuperAdmin().username()));
    }

    @Test
    void listAllProjectUsers() {
        UserResponse user = builder.givenSuperAdmin();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, "superadmin", WRITE);

        assertThat(userService.listAll(project)).contains(user);
    }

    @Test
    void listLayers() {
        UserResponse user = builder.givenUserAclRead();
        User anotherUserInProject = builder.getUserEntity(USER_ACL_READ);
        User anotherUserNotInProject = builder.getUserEntity(USER_ACL_WRITE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.username(), WRITE);
        builder.addUserToProject(project, anotherUserInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project)
            .stream()
            .map(UserResponse::id))
            .contains(user.id(), anotherUserInProject.getId())
            .doesNotContain(anotherUserNotInProject.getId());
    }

    @WithMockUser(ACL_USER_NO_ACL)
    @Test
    void listLayersWithProjectWithPrivateAdminLayer() {
        UserResponse user = builder.givenAclUserNoAcl();
        User adminInProject = builder.getUserEntity(USER_ACL_ADMIN);

        Project project = builder.givenAProject();
        project.setHideAdminsLayers(true);

        builder.addUserToProject(project, user.username(), WRITE);
        builder.addUserToProject(project, adminInProject.getUsername(), ADMINISTRATION);

        assertThat(userService.listLayers(project)
            .stream()
            .map(UserResponse::id))
            .hasSize(1)
            .contains(user.id())
            .doesNotContain(adminInProject.getId());
    }

    @WithMockUser(ACL_USER_NO_ACL)
    @Test
    void listLayersWithProjectWithPrivateUserLayer() {
        UserResponse user = builder.givenAclUserNoAcl();
        User userInProject = builder.getUserEntity(USER_ACL_READ);

        Project project = builder.givenAProject();
        project.setHideUsersLayers(true);

        builder.addUserToProject(project, user.username(), WRITE);
        builder.addUserToProject(project, userInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project)
            .stream()
            .map(UserResponse::id))
            .hasSize(1)
            .contains(user.id())
            .doesNotContain(userInProject.getId());
    }

    @WithMockUser(ACL_USER_NO_ACL)
    @Test
    void listLayersWithProjectWithPrivateUserLayerWithProjectAdminRole() {
        UserResponse user = builder.givenAclUserNoAcl();
        User userInProject = builder.getUserEntity(USER_ACL_READ);

        Project project = builder.givenAProject();
        project.setHideUsersLayers(true);

        builder.addUserToProject(project, user.username(), ADMINISTRATION);
        builder.addUserToProject(project, userInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project)
            .stream()
            .map(UserResponse::id))
            .hasSize(2)
            .contains(user.id(), userInProject.getId());
    }

    @Test
    void listOnlineUser() {
        UserResponse userOnline = builder.givenAclUserNoAcl();
        UserResponse userOffline = builder.givenUserAclRead();

        assertThat(userService.getAllOnlineUsers()).isEmpty();
        givenALastConnection(builder.getUserEntity(userOnline.username()), null, new Date());

        assertThat(userService.getAllOnlineUsers()).contains(userOnline)
            .doesNotContain(userOffline);
    }

    @Test
    void listOnlineUserForProject() {
        UserResponse userOnline = builder.givenAclUserNoAcl();
        User userOnlineButOnDifferentProject = builder.getUserEntity(USER_ACL_WRITE);
        User userOffline = builder.getUserEntity(USER_ACL_READ);

        Project project = builder.givenAProject();
        Project anotherProject = builder.givenAProject();

        givenALastConnection(userOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(builder.getUserEntity(userOnline.username()), project.getId(),
            DateUtils.addSeconds(new Date(), -15));
        givenALastConnection(
            userOnlineButOnDifferentProject,
            anotherProject.getId(),
            DateUtils.addSeconds(new Date(), -10)
        );

        assertThat(userService.getAllOnlineUserIds(project)).contains(userOnline.id())
            .doesNotContain(userOnlineButOnDifferentProject.getId(), userOffline.getId());
        assertThat(userService.getAllOnlineUsers(project)).contains(builder.getUserEntity(userOnline.username()))
            .doesNotContain(userOnlineButOnDifferentProject, userOffline);
    }

    @Test
    void listFriendUsers() {
        UserResponse user = builder.givenAclUserNoAcl();
        UserResponse userFriend = builder.givenUserAclRead();
        UserResponse userNotFriend = builder.givenUserAclWrite();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.username(), READ);
        builder.addUserToProject(project, userFriend.username(), READ);

        assertThat(userService.getAllFriendsUsers(builder.getUserEntity(user.username()))).contains(userFriend)
            .doesNotContain(userNotFriend);
    }

    @Test
    void listFriendUsersOffline() {
        UserResponse user = builder.givenAclUserNoAcl();
        User userFriendOnline = builder.getUserEntity(USER_ACL_READ);
        User userFriendOffline = builder.getUserEntity(USER_ACL_WRITE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.username(), READ);
        builder.addUserToProject(project, userFriendOnline.getUsername(), READ);
        builder.addUserToProject(project, userFriendOffline.getUsername(), READ);

        givenALastConnection(userFriendOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(userFriendOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));

        assertThat(userService.getAllFriendsUsersOnline(builder.getUserEntity(user.username())))
            .contains(builder.givenUserAclRead())
            .doesNotContain(builder.givenUserAclWrite());
    }

    @Test
    void listFriendUsersOfflineOnAProject() {
        UserResponse user = builder.givenAclUserNoAcl();
        User userFriendOnline = builder.getUserEntity(USER_ACL_READ);
        User userFriendOnlineButOnAnotherProject = builder.getUserEntity(USER_ACL_WRITE);

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.username(), READ);
        builder.addUserToProject(project, userFriendOnline.getUsername(), READ);
        builder.addUserToProject(project, userFriendOnlineButOnAnotherProject.getUsername(), READ);

        givenALastConnection(
            userFriendOnlineButOnAnotherProject,
            builder.givenAProject().getId(),
            DateUtils.addSeconds(new Date(), -15)
        );
        givenALastConnection(userFriendOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));

        assertThat(userService.getAllFriendsUsersOnline(builder.getUserEntity(user.username()), project))
            .contains(builder.givenUserAclRead())
            .doesNotContain(builder.givenUserAclWrite());
    }

    @Test
    void listOnlineUserForProjectWitTheirActivities() {
        UserResponse userOnline = builder.givenAclUserNoAcl();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userOnline.username());

        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            builder.getUserEntity(userOnline.username()),
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        PersistentImageConsultation consultation = givenAPersistentImageConsultation(
            builder.getUserEntity(userOnline.username()),
            builder.givenAnImageInstance(project),
            new Date()
        );

        List<JsonObject> allOnlineUserWithTheirPositions = userService.getUsersWithLastActivities(project);
        assertThat(allOnlineUserWithTheirPositions).hasSize(1);
        assertThat(allOnlineUserWithTheirPositions.get(0).get("id")).isEqualTo(userOnline.id());
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastImageId")).isEqualTo(consultation.getImage());
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastImageName")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastConnection")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.get(0).get("frequency")).isEqualTo(1);
    }

    @Test
    void listOnlineUserForProjectWitTheirPosition() {
        UserResponse userOnline = builder.givenAclUserNoAcl();
        User userOnlineButOnDifferentProject = builder.getUserEntity(USER_ACL_WRITE);
        User userOffline = builder.getUserEntity(USER_ACL_READ);

        Project project = builder.givenAProject();
        Project anotherProject = builder.givenAProject();

        givenALastConnection(userOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(builder.getUserEntity(userOnline.username()), project.getId(),
            DateUtils.addSeconds(new Date(), -15));
        givenALastConnection(
            userOnlineButOnDifferentProject,
            anotherProject.getId(),
            DateUtils.addSeconds(new Date(), -10)
        );

        givenAPersistentUserPosition(
            DateUtils.addSeconds(new Date(), -15), builder.getUserEntity(userOnline.username()),
            builder.givenANotPersistedSliceInstance(
                builder.givenAnImageInstance(project),
                builder.givenAnAbstractSlice()
            ), UserPositionServiceTests.USER_VIEW
        );

        List<JsonObject> allOnlineUserWithTheirPositions = userService.getAllOnlineUserWithTheirPositions(project);
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnline.id()))
            .findFirst()).isPresent();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnline.id()))
            .findFirst()
            .get()
            .get("position")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnlineButOnDifferentProject.getId()))
            .findFirst()).isEmpty();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOffline.getId()))
            .findFirst()).isEmpty();
    }

    PersistentUserPosition givenAPersistentUserPosition(
        Date creation,
        User user,
        SliceInstance sliceInstance,
        AreaDTO areaDTO
    ) {
        return userPositionService.add(
            creation,
            user.getId(),
            sliceInstance,
            sliceInstance.getImage(),
            areaDTO,
            1,
            5.0,
            false
        );
    }

    @Test
    void listUserResumeActivities() {
        UserResponse userOnline = builder.givenAclUserNoAcl();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, userOnline.username());

        PersistentProjectConnection firstConnection = givenAPersistentConnectionInProject(
            builder.getUserEntity(userOnline.username()),
            project,
            DateUtils.addDays(new Date(), -15)
        );
        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            builder.getUserEntity(userOnline.username()),
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        givenAPersistentImageConsultation(builder.getUserEntity(userOnline.username()),
            builder.givenAnImageInstance(project),
            new Date());

        JsonObject data = userService.getResumeActivities(project, userOnline);

        assertThat(data.getJSONAttrDate("firstConnection")).isEqualTo(firstConnection.getCreated());
        assertThat(data.getJSONAttrDate("lastConnection")).isEqualTo(lastConnection.getCreated());
        assertThat(data.getJSONAttrInteger("totalAnnotations")).isEqualTo(0);
        assertThat(data.getJSONAttrInteger("totalConnections")).isEqualTo(2);
        assertThat(data.getJSONAttrInteger("totalConsultations")).isEqualTo(1);
        assertThat(data.getJSONAttrInteger("totalAnnotationSelections")).isEqualTo(0);
    }

    // TODO: IAM Account

    @Test
    void addUserToProject() {
        UserResponse user = builder.givenUserAclRead();
        Project project = builder.givenAProject();

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isFalse();

        projectMemberService.addUserToProject(user.username(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();

        projectMemberService.addUserToProject(user.username(), project, true);

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();
    }

    @Test
    void removeUserFromProject() {
        UserResponse user = builder.givenUserAclRead();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user.username(), project, true);

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user.username(), user.id(), project, true);

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user.username(), user.id(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isFalse();
    }

    @Test
    void removeOntologyRightWhenRemovingUserFromProject() {
        UserResponse user = builder.givenUserAclRead();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user.username(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user.username(), user.id(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isFalse();
    }

    @Test
    void removeOntologyRightWhenRemovingUserFromProjectKeepRightIfUserHasAnotherProjectWithOntology() {
        UserResponse user = builder.givenUserAclRead();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user.username(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isTrue();

        Project projectWithSameOntology = builder.givenAProject();
        projectWithSameOntology.setOntology(project.getOntology());
        projectMemberService.addUserToProject(user.username(), projectWithSameOntology, false);

        projectMemberService.deleteUserFromProject(user.username(), user.id(), project, false);

        assertThat(permissionService.hasACLPermission(project, user.username(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.username(), READ)).isTrue();
    }
}
