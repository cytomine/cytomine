package be.cytomine.config.security;

import java.util.Optional;

/**
 * Holds the request headers needed to re-authenticate the current request against the
 * repository service, so they can be forwarded by {@link be.cytomine.controller.repository.RepositoryClient}.
 * Set and cleared explicitly by {@link IncomingAuthorizationFilter} around the synchronous
 * portion of request handling only, so it is never left stale on a reused worker thread.
 *
 * <p>All four fields are needed, not just Authorization: a CYTOMINE-scheme signature is an
 * HMAC over {@code method + contentMd5 + contentType + date}, so repository must receive the
 * same date/content-MD5/content-type the browser signed, or its recomputed signature won't match.
 */
public final class IncomingAuthorizationContext {

    public record Headers(String authorization, String date, String contentMd5, String contentType) {
    }

    private static final ThreadLocal<Headers> HEADERS = new ThreadLocal<>();

    private IncomingAuthorizationContext() {
    }

    static void set(Headers headers) {
        HEADERS.set(headers);
    }

    static void clear() {
        HEADERS.remove();
    }

    public static Optional<Headers> get() {
        return Optional.ofNullable(HEADERS.get());
    }
}
