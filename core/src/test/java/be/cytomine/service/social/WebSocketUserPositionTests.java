package be.cytomine.service.social;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@ExtendWith(MockitoExtension.class)
public class WebSocketUserPositionTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    WebSocketUserPositionHandler webSocketUserPositionHandler;

    @AfterEach
    public void cleanSessions() {
        WebSocketUserPositionHandler.sessions = new ConcurrentHashMap<>();
        WebSocketUserPositionHandler.sessionsTracked = new ConcurrentHashMap<>();
        WebSocketUserPositionHandler.sessionsBroadcast = new ConcurrentHashMap<>();
    }

    @Test
    public void createSessionForNotConnectedUser() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes("54", "imageId", "false"));

        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get("54/imageId")).isNull();
        assertThat(WebSocketUserPositionHandler.sessions.get("54")).isNull();
        webSocketUserPositionHandler.afterConnectionEstablished(session);
        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get("54/imageId")).isNull();
        assertThat(WebSocketUserPositionHandler.sessions.get("54")).isNotEmpty();
    }

    @Test
    public void createBroadcastSessionForNotConnectedUser() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes("54", "imageId", "true"));

        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get("54/imageId")).isNull();
        assertThat(WebSocketUserPositionHandler.sessions.get("54")).isNull();
        webSocketUserPositionHandler.afterConnectionEstablished(session);
        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get("54/imageId")).isNotNull();
        assertThat(WebSocketUserPositionHandler.sessions.get("54")).isNotEmpty();
    }

    @Test
    public void addSessionForAlreadyConnectedUser() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);
        connectSession(sessionDecorator, "54", "imageId", "false");
        connectSession(sessionDecorator, "89", "imageId", "false");
        assertThat(WebSocketUserPositionHandler.sessions.get("54").length).isEqualTo(1);
        assertThat(WebSocketUserPositionHandler.sessions.get("89").length).isEqualTo(1);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes("54", "imageId", "false"));

        webSocketUserPositionHandler.afterConnectionEstablished(session);
        assertThat(WebSocketUserPositionHandler.sessions.get("54").length).isEqualTo(2);
        assertThat(WebSocketUserPositionHandler.sessions.get("89").length).isEqualTo(1);
    }

    @Test
    public void addTrackSessionToNotTrackedUser() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId + "/" + imageInstanceId;

        connectSession(sessionDecorator, userId, imageInstanceId, "true");

        // Should have created a broadcast session
        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get(userAndImageId)).isNotNull();

        // Ask for follow the broadcast session
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes(userId, imageInstanceId, "false"));
        when(session.getId()).thenReturn("1234");
        when(sessionDecorator.getId()).thenReturn("1234");
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId));

        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);
    }

    @Test
    public void addTrackSessionToAlreadyTrackedUser() {
        ConcurrentWebSocketSessionDecorator followerSession = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId + "/" + imageInstanceId;

        connectSession(followerSession, userId, imageInstanceId, "true");
        initFollowingSession(userAndImageId, broadcastSession, followerSession);

        //Should have added session to sessions tracked
        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);

        WebSocketSession session = mock(WebSocketSession.class);
        connectSession(session, userId, imageInstanceId, "false");

        when(session.getId()).thenReturn("1234");
        when(followerSession.getId()).thenReturn("5678");

        // Ask a new follow on the broadcast session
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId));

        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(2);
    }

    @Test
    public void addTrackSessionWhoIsAlreadyTracking() {
        ConcurrentWebSocketSessionDecorator followerSession = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);
        when(followerSession.getId()).thenReturn("5678");

        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId + "/" + imageInstanceId;

        connectSession(followerSession, userId, imageInstanceId, "true");
        initFollowingSession(userAndImageId, broadcastSession, followerSession);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("1234");
        connectSession(session, userId, imageInstanceId, "false");

        // Ask a new follow on the broadcast session
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId));

        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(2);

        // Ask a follow on the broadcast session with already tracking session
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId));
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(2);
    }

    @Test
    public void addSomeTrackSessionsToAlreadyTrackedUser() {
        ConcurrentWebSocketSessionDecorator followerSession1 = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator followerSession2 = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator followerSession3 = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId1 = builder.givenAUser().getId().toString();
        String userId2 = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId1 + "/" + imageInstanceId;
        initFollowingSession(userAndImageId, broadcastSession, followerSession1);

        //Should have added session to sessions tracked
        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);

        // Simulate that user is connected to Cytomine with 2 sessions
        connectSession(followerSession2, userId2, imageInstanceId, "false");
        connectSession(followerSession3, userId2, imageInstanceId, "false");
        when(followerSession1.getId()).thenReturn("1");
        when(followerSession2.getId()).thenReturn("2");
        when(followerSession3.getId()).thenReturn("3");

        // Ask for session 2 only to follow the broadcast session
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes(userId2, imageInstanceId, "false"));
        when(session.getId()).thenReturn("2");

        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId1));
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(2);

        // Ask for session 3 only to follow the broadcast session
        when(session.getId()).thenReturn("3");
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId1));
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(3);
    }

    @Test
    public void removeTrackingSessionsFromTrackedSessions() throws Exception {
        ConcurrentWebSocketSessionDecorator followerSession = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId + "/" + imageInstanceId;

        connectSession(followerSession, userId, imageInstanceId, "false");
        initFollowingSession(userAndImageId, broadcastSession, followerSession);

        // Broadcast session should be followed by follower session
        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes(userId, imageInstanceId, "false"));
        when(session.getId()).thenReturn("1234");
        when(followerSession.getId()).thenReturn("1234");

        // Close the followerSession (by calling afterConnectionClosed with mock session with same session id)
        webSocketUserPositionHandler.afterConnectionClosed(session, CloseStatus.NO_STATUS_CODE);

        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(0);
    }

    @Test
    public void removeBroadcastingSessionsFromTrackedSessions() throws Exception {
        ConcurrentWebSocketSessionDecorator followerSession = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        String userAndImageId = userId + "/" + imageInstanceId;

        connectSession(followerSession, userId, imageInstanceId, "false");
        initFollowingSession(userAndImageId, broadcastSession, followerSession);

        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(sessionAttributes(userId, imageInstanceId, "true"));
        when(session.getId()).thenReturn("1234");
        when(followerSession.getId()).thenReturn("5678");
        when(broadcastSession.getId()).thenReturn("1234");

        // Close the broadcastSession (by calling afterConnectionClosed with mock session with same session id)
        webSocketUserPositionHandler.afterConnectionClosed(session, CloseStatus.NO_STATUS_CODE);

        assertThat(WebSocketUserPositionHandler.sessionsBroadcast.get(userAndImageId)).isNull();
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession)).isNull();
    }

    @Test
    public void updatePositionOfTrackedUserSendMessageWorks() throws IOException {
        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();

        WebSocketSession session = mock(WebSocketSession.class);
        connectSession(session, userId, imageInstanceId, "true");

        when(session.getAttributes()).thenReturn(sessionAttributes(userId, imageInstanceId, "false"));
        when(session.getId()).thenReturn("1234");
        webSocketUserPositionHandler.handleMessage(session, new TextMessage(userId));

        when(session.isOpen()).thenReturn(true);
        doNothing().when(session).sendMessage(new TextMessage("position"));
        assertDoesNotThrow(() -> webSocketUserPositionHandler.sendPositionToFollowers(
            userId,
            imageInstanceId,
            "position"
        ));
        verify(session, Mockito.timeout(2000).times(1)).sendMessage(new TextMessage("position"));
    }

    @Test
    public void updatePositionOfNotTrackedUserDoNothing() {
        String userId = builder.givenAUser().getId().toString();
        String imageInstanceId = builder.givenAnImageInstance().getId().toString();
        assertDoesNotThrow(() -> webSocketUserPositionHandler.sendPositionToFollowers(
            userId,
            imageInstanceId,
            "position"
        ));
    }

    @Test
    public void removeSessionIfConnectionClosed() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        ConcurrentWebSocketSessionDecorator followerSession = mock(ConcurrentWebSocketSessionDecorator.class);
        ConcurrentWebSocketSessionDecorator broadcastSession = mock(ConcurrentWebSocketSessionDecorator.class);

        String userId = builder.givenAUser().getId().toString();
        String userAndImageId = userId + "/imageId";
        connectSession(session, userId, "imageId", "false");
        initFollowingSession(userAndImageId, broadcastSession, followerSession);

        assertThat(WebSocketUserPositionHandler.sessions.get(userId).length).isEqualTo(1);
        ConcurrentWebSocketSessionDecorator createdSession = WebSocketUserPositionHandler.sessionsBroadcast.get(
            userAndImageId);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(1);

        when(session.getId()).thenReturn("1");
        when(followerSession.getId()).thenReturn("1");
        // Close the session (by calling afterConnectionClosed with mock session with same session id)
        webSocketUserPositionHandler.afterConnectionClosed(session, CloseStatus.NO_STATUS_CODE);

        assertThat(WebSocketUserPositionHandler.sessions.get(userId).length).isEqualTo(0);
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(createdSession).length).isEqualTo(0);
    }


    private void connectSession(WebSocketSession session, String userId, String imageId, String broadcast) {
        when(session.getAttributes()).thenReturn(Map.of("userId", userId, "imageId", imageId, "broadcast", broadcast));
        webSocketUserPositionHandler.afterConnectionEstablished(session);
    }

    private void initFollowingSession(
        String userAndImageId,
        ConcurrentWebSocketSessionDecorator broadcastSession,
        ConcurrentWebSocketSessionDecorator followerSession
    ) {
        WebSocketUserPositionHandler.sessionsBroadcast.put(userAndImageId, broadcastSession);
        WebSocketUserPositionHandler.sessionsTracked.put(
            broadcastSession,
            new ConcurrentWebSocketSessionDecorator[]{followerSession}
        );
        assertThat(WebSocketUserPositionHandler.sessionsTracked.get(broadcastSession).length).isEqualTo(1);
    }

    private Map<String, Object> sessionAttributes(String userId, String imageId, String broadcast) {
        return Map.of("userId", userId, "imageId", imageId, "broadcast", broadcast);
    }
}
