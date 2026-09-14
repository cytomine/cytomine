package be.cytomine.config.security;

import java.util.Optional;

/**
 * Holds the Authorization header of the request currently being handled by this thread, so it
 * can be forwarded to the repository service. Unlike {@link org.springframework.web.context.request.RequestContextHolder},
 * this is set and cleared explicitly by {@link IncomingAuthorizationFilter} around the synchronous
 * portion of request handling only, so it is never left stale on a Tomcat worker thread that gets
 * reused while an unrelated async request (e.g. a StreamingResponseBody download) is still in flight.
 */
public final class IncomingAuthorizationContext {

    private static final ThreadLocal<String> AUTHORIZATION = new ThreadLocal<>();

    private IncomingAuthorizationContext() {
    }

    static void set(String authorization) {
        AUTHORIZATION.set(authorization);
    }

    static void clear() {
        AUTHORIZATION.remove();
    }

    public static Optional<String> get() {
        return Optional.ofNullable(AUTHORIZATION.get());
    }
}
