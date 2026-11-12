package be.cytomine.unit.service.security;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.cytomine.common.repository.http.UserHttpContract;
import org.cytomine.common.repository.model.command.payload.response.UserResponse;
import org.cytomine.common.repository.model.user.payload.CreateUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import be.cytomine.domain.security.User;
import be.cytomine.mapper.UserMapper;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.security.UserService;
import be.cytomine.utils.AuthenticationSuccessListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationSuccessListenerTest {

    private static final String USERNAME = "flora";

    private final UUID userReference = UUID.randomUUID();

    @Mock
    private CurrentRoleService currentRoleService;

    @Mock
    private UserService userService;

    @Mock
    private UserHttpContract userHttpContract;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private UserMapper userMapper;

    private AuthenticationSuccessListener listener;

    @BeforeEach
    public void setUp() {
        listener = new AuthenticationSuccessListener(currentRoleService, userService, userHttpContract,
            projectMemberService, userMapper);
    }

    @DisplayName("A user already linked to its token subject is synchronized with its claims")
    @Test
    public void linkedUserIsSynchronizedWithTokenClaims() {
        User user = user();
        when(userService.findUserByReference(userReference.toString())).thenReturn(Optional.of(user));

        listener.onApplicationEvent(
            authenticationEventOf(USERNAME, List.of("alpha"), List.of("join-token-1")));

        verify(userService).syncCumulativeRole(user, Set.of());
        verify(projectMemberService).syncProjectMembership(user, List.of("alpha"));
        verify(projectMemberService).syncJoinTokenMembership(user, List.of("join-token-1"));
    }

    @DisplayName("A user known only by its token subject name is linked and synchronized")
    @Test
    public void unlinkedUserIsLinkedToItsTokenSubject() {
        User user = user();
        when(userService.findUserByUsername(userReference.toString())).thenReturn(Optional.of(user));
        when(userService.findUserByReference(userReference.toString())).thenReturn(Optional.empty());

        listener.onApplicationEvent(authenticationEventOf(USERNAME, List.of(), List.of()));

        verify(userService).save(user);
        verify(userService).syncCumulativeRole(user, Set.of());
        verify(projectMemberService).syncProjectMembership(user, List.of());
        verify(projectMemberService).syncJoinTokenMembership(user, List.of());
    }

    @DisplayName("An unknown user is self-registered then synchronized")
    @Test
    public void unknownUserIsSelfRegisteredAndSynchronized() {
        User savedUser = user();
        savedUser.setReference(userReference.toString());
        when(userService.findUserByUsername(userReference.toString())).thenReturn(Optional.empty());
        when(userService.findUserByReference(userReference.toString()))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(savedUser));
        when(userMapper.map(savedUser)).thenReturn(userResponse());

        listener.onApplicationEvent(authenticationEventOf(USERNAME, List.of("alpha"), List.of()));

        verify(userHttpContract).selfRegister(any(CreateUser.class));
        verify(projectMemberService).syncProjectMembership(savedUser, List.of("alpha"));
        verify(projectMemberService).syncJoinTokenMembership(savedUser, List.of());
        verify(currentRoleService).hasCurrentUserAdminRole(any(UserResponse.class));
    }

    private User user() {
        User user = new User();
        user.setUsername(USERNAME);
        return user;
    }

    private UserResponse userResponse() {
        return new UserResponse(1L, USERNAME, null, Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), false, Optional.empty(), Optional.empty(), Optional.empty(), null,
            Optional.empty(), Optional.empty(), Set.of());
    }

    private AuthenticationSuccessEvent authenticationEventOf(String username, List<String> projects,
        List<String> joinTokens) {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .header("typ", "JWT")
            .subject(userReference.toString())
            .claim("preferred_username", username)
            .claim("projects", projects)
            .claim("lti_join_tokens", joinTokens)
            .build();
        return new AuthenticationSuccessEvent(new JwtAuthenticationToken(jwt));
    }
}
