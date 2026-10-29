package be.cytomine.unit.service.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.security.IamProjectMembershipService;
import be.cytomine.utils.AuthenticationSuccessListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the reconciliation of the project membership of a user with the projects provisioned in IAM,
 * performed by {@link AuthenticationSuccessListener} on every authenticated request.
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationSuccessListenerTest {

    private static final String INTERNAL_USERNAME = "flora";

    private static final String EXTERNAL_USERNAME = "flora@lifescience-ri.eu";

    private final UUID userReference = UUID.randomUUID();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CurrentRoleService currentRoleService;

    @Mock
    private SecUserSecRoleRepository secSecUserSecRoleRepository;

    @Mock
    private SecRoleRepository secRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IamProjectMembershipService iamProjectMembershipService;

    private AuthenticationSuccessListener listener;

    @BeforeEach
    public void setUp() {
        listener = new AuthenticationSuccessListener(applicationContext, currentRoleService,
            secSecUserSecRoleRepository, secRoleRepository, null, userRepository, projectRepository,
            projectMemberService, userMapper, iamProjectMembershipService);
        // the listener calls itself through the application context so that transactional methods run
        // through their Spring proxy
        org.mockito.Mockito.doReturn(listener)
            .when(applicationContext).getBean(AuthenticationSuccessListener.class);
    }

    @DisplayName("The project membership of a local user is not synchronized with IAM")
    @Test
    public void localUserMembershipIsNotSynchronizedWithIam() {
        User user = internalUser();
        when(userRepository.findByReference(userReference.toString())).thenReturn(Optional.of(user));

        listener.onApplicationEvent(authenticationEventOf(INTERNAL_USERNAME));

        verify(iamProjectMembershipService, never()).findProjects(anyString());
        verify(projectMemberService, never()).addUserToProjectWithAdmin(anyString(), any(Project.class),
            anyBoolean());
        verify(projectMemberService, never()).deleteUserFromProjectWithAdmin(any(User.class), any(Project.class),
            anyBoolean());
    }

    @DisplayName("The membership of an external user matching IAM provisioning is left untouched")
    @Test
    public void externalUserMembershipMatchingIamIsLeftUntouched() {
        Project project = project("alpha", 1L);
        User user = externalUser();
        when(userRepository.findByReference(userReference.toString())).thenReturn(Optional.of(user));
        when(iamProjectMembershipService.isExternal(EXTERNAL_USERNAME)).thenReturn(true);
        when(iamProjectMembershipService.findProjects(EXTERNAL_USERNAME)).thenReturn(List.of("alpha"));
        when(projectRepository.findByNameIn(List.of("alpha"))).thenReturn(List.of(project));
        when(projectRepository.findAllProjectForUser(EXTERNAL_USERNAME)).thenReturn(List.of(project));

        listener.onApplicationEvent(authenticationEventOf(EXTERNAL_USERNAME));

        verify(projectMemberService, never()).addUserToProjectWithAdmin(anyString(), any(Project.class),
            anyBoolean());
        verify(projectMemberService, never()).deleteUserFromProjectWithAdmin(any(User.class), any(Project.class),
            anyBoolean());
    }

    @DisplayName("The membership of an external user not present in IAM provisioning is removed")
    @Test
    public void membershipOfExternalUserAbsentFromIamIsRemoved() {
        Project alphaProject = project("alpha", 1L);
        Project betaProject = project("beta", 2L);
        User user = externalUser();
        when(userRepository.findByReference(userReference.toString())).thenReturn(Optional.of(user));
        when(iamProjectMembershipService.isExternal(EXTERNAL_USERNAME)).thenReturn(true);
        when(iamProjectMembershipService.findProjects(EXTERNAL_USERNAME)).thenReturn(List.of("alpha"));
        when(projectRepository.findByNameIn(List.of("alpha"))).thenReturn(List.of(alphaProject));
        when(projectRepository.findAllProjectForUser(EXTERNAL_USERNAME)).thenReturn(
            List.of(alphaProject, betaProject));

        listener.onApplicationEvent(authenticationEventOf(EXTERNAL_USERNAME));

        verify(projectMemberService).deleteUserFromProjectWithAdmin(user, betaProject, false);
        verify(projectMemberService, never()).addUserToProjectWithAdmin(anyString(), any(Project.class),
            anyBoolean());
    }

    @DisplayName("The IAM provisioning of an external user without membership in core is granted")
    @Test
    public void iamProvisioningAbsentFromCoreMembershipIsGranted() {
        Project alphaProject = project("alpha", 1L);
        Project betaProject = project("beta", 2L);
        User user = externalUser();
        when(userRepository.findByReference(userReference.toString())).thenReturn(Optional.of(user));
        when(iamProjectMembershipService.isExternal(EXTERNAL_USERNAME)).thenReturn(true);
        when(iamProjectMembershipService.findProjects(EXTERNAL_USERNAME)).thenReturn(List.of("alpha", "beta"));
        when(projectRepository.findByNameIn(List.of("alpha", "beta"))).thenReturn(
            List.of(alphaProject, betaProject));
        when(projectRepository.findAllProjectForUser(EXTERNAL_USERNAME)).thenReturn(List.of(alphaProject));

        listener.onApplicationEvent(authenticationEventOf(EXTERNAL_USERNAME));

        verify(projectMemberService).addUserToProjectWithAdmin(EXTERNAL_USERNAME, betaProject, false);
        verify(projectMemberService, never()).deleteUserFromProjectWithAdmin(any(User.class), any(Project.class),
            anyBoolean());
    }

    private User internalUser() {
        User user = new User();
        user.setUsername(INTERNAL_USERNAME);
        user.setReference(userReference.toString());
        return user;
    }

    private User externalUser() {
        User user = new User();
        user.setUsername(EXTERNAL_USERNAME);
        user.setReference(userReference.toString());
        return user;
    }

    private Project project(String name, long id) {
        Project project = new Project();
        project.setName(name);
        project.setId(id);
        return project;
    }

    private AuthenticationSuccessEvent authenticationEventOf(String username) {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .header("typ", "JWT")
            .subject(userReference.toString())
            .claim("preferred_username", username)
            .build();
        return new AuthenticationSuccessEvent(new JwtAuthenticationToken(jwt));
    }
}
