package be.cytomine.common.config.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public final class CytomineAuthenticationSupport {

    public static final String SCHEME = "CYTOMINE";

    private CytomineAuthenticationSupport() {
    }

    public record Credentials(String accessKey, String signature) {
    }

    public static Optional<Credentials> parse(String authorization) {
        if (authorization == null
            || !authorization.startsWith(SCHEME)
            || !authorization.contains(" ")
            || !authorization.contains(":")) {
            return Optional.empty();
        }
        String accessKey = authorization.substring(authorization.indexOf(" ") + 1, authorization.indexOf(":"));
        String signature = authorization.substring(authorization.indexOf(":") + 1);
        return Optional.of(new Credentials(accessKey, signature));
    }

    public static String buildAuthorizationHeader(String accessKey, String signature) {
        return SCHEME + " " + accessKey + ":" + signature;
    }

    public static String generateSignature(String method, String contentMd5, String contentType, String date,
        String privateKey) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String canonicalHeaders = method + "\n" + contentMd5 + "\n" + contentType + "\n" + date;

        SecretKeySpec signingKey = new SecretKeySpec(privateKey.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(new String(canonicalHeaders.getBytes(), "UTF-8").getBytes());

        byte[] signatureBytes = Base64.encodeBase64(rawHmac);
        return new String(signatureBytes);
    }

    public static boolean matchesSignature(String method, String contentMd5, String contentType, String date,
        String privateKey, String providedSignature)
        throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        if (providedSignature.equals(generateSignature(method, contentMd5, contentType, date, privateKey))) {
            return true;
        }
        // the java client does not set content-type,
        // so we override the header to application/json BEFORE this authentication.
        // So the client thinks content-type is "" while spring boot set it to application/json.
        // In order to match the client signature, we generate it with an empty value.
        // => it would be better to improve the java client to set a valid content type.
        return providedSignature.equals(generateSignature(method, contentMd5, "", date, privateKey));
    }
}
