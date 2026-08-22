package com.cytomine.keycloak.lti;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Verifies the OAuth 1.0a HMAC-SHA1 signature LTI 1.1 launches are signed
 * with (RFC 5849 section 3.4.2 / OAuth Core 1.0a). LTI 1.1 never uses a
 * token secret, so the signing key is always "consumerSecret&amp;".
 *
 * This is deliberately dependency-free (no external OAuth library) so the
 * scaffold has no extra runtime deps beyond what Keycloak already ships.
 */
public final class OAuth1LaunchValidator {

    private OAuth1LaunchValidator() {}

    /**
     * @param httpMethod   e.g. "POST"
     * @param requestUrl   the exact URL the LMS launched to, no query string
     *                     (scheme+host+port+path must match exactly what was
     *                     signed - reverse proxies/hostname mismatches are
     *                     the most common cause of false verification failures)
     * @param params       all launch parameters (form-encoded body params),
     *                     including oauth_signature - it is excluded automatically
     * @param consumerSecret the shared secret for the oauth_consumer_key in params
     */
    public static boolean verify(String httpMethod, String requestUrl,
                                  Map<String, List<String>> params, String consumerSecret) {
        String providedSignature = firstOrNull(params.get("oauth_signature"));
        if (providedSignature == null) {
            return false;
        }

        String baseString = buildSignatureBaseString(httpMethod, requestUrl, params);
        String signingKey = percentEncode(consumerSecret) + "&"; // no token secret in LTI

        String computed = hmacSha1Base64(baseString, signingKey);
        return constantTimeEquals(computed, providedSignature);
    }

    private static String buildSignatureBaseString(String httpMethod, String requestUrl,
                                                     Map<String, List<String>> params) {
        SortedMap<String, List<String>> normalized = new TreeMap<>();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            if ("oauth_signature".equals(e.getKey())) continue;
            normalized.put(e.getKey(), e.getValue());
        }

        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, List<String>> e : normalized.entrySet()) {
            List<String> values = e.getValue().stream().sorted().toList();
            for (String v : values) {
                if (!first) paramString.append("&");
                paramString.append(percentEncode(e.getKey())).append("=").append(percentEncode(v));
                first = false;
            }
        }

        return httpMethod.toUpperCase() + "&"
                + percentEncode(requestUrl) + "&"
                + percentEncode(paramString.toString());
    }

    private static String hmacSha1Base64(String baseString, String signingKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] raw = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute OAuth1 signature", e);
        }
    }

    /** RFC 3986 unreserved-char percent-encoding, as OAuth 1.0a requires (stricter than URLEncoder). */
    private static String percentEncode(String value) {
        if (value == null) return "";
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] ab = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ab.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < ab.length; i++) diff |= ab[i] ^ bb[i];
        return diff == 0;
    }

    private static String firstOrNull(List<String> values) {
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }
}
