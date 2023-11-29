package io.github.ya_b.registry.client.http;

import io.github.ya_b.registry.client.exception.HttpCodeErrorException;
import io.github.ya_b.registry.client.exception.RegistryException;
import io.github.ya_b.registry.client.http.auth.Authenticator;
import io.github.ya_b.registry.client.http.resp.CatalogResp;
import io.github.ya_b.registry.client.http.resp.TagsResp;
import io.github.ya_b.registry.client.image.ImageMediaType;
import io.github.ya_b.registry.client.image.registry.ManifestHttp;
import io.github.ya_b.registry.client.name.Reference;
import io.github.ya_b.registry.client.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RegistryApi {
    
    private static final String BASE = "%s/v2/";
    private static final String CATALOG = "%s/v2/_catalog?n=%s&last=%s";
    private static final String TAGS = "%s/v2/%s/tags/list";
    private static final String MANIFEST = "%s/v2/%s/manifests/%s";
    private static final String BLOB = "%s/v2/%s/blobs/%s";
    private static final String BLOB_UPLOAD = "%s/v2/%s/blobs/uploads/";
    private static final Pattern AUTH_URL_PATTERN = Pattern.compile("Bearer realm=\"(.*?)\",service=\"(.*?)\"");

    public int base(String endpoint) throws IOException {
        try (Response response = HttpClient.execute(HttpClient.METHOD_HEAD, String.format(BASE, endpoint), null, null)) {
            int code = response.code();
            if (code != 401) {
                return code;
            }
            String auth = response.header("Www-Authenticate");
            if (auth == null) return code;
            Matcher matcher = AUTH_URL_PATTERN.matcher(auth);
            if (!matcher.find()) return code;
            Authenticator.instance().setAuthUrl(new Authenticator.AuthUrl(matcher.group(1), matcher.group(2)));
            return code;
        }
    }

    public Optional<String> digest(Reference reference, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        headers.put(HttpHeaders.ACCEPT, "application/vnd.docker.distribution.manifest.v1+json," +
                "application/vnd.docker.distribution.manifest.v1+prettyjws," +
                "application/vnd.docker.distribution.manifest.v2+json," +
                "application/vnd.docker.distribution.manifest.list.v2+json," +
                "application/vnd.oci.image.manifest.v1+json," +
                "application/vnd.oci.image.index.v1+json");
        String url = String.format(MANIFEST, reference.getEndpoint(), reference.getName(), reference.getTag());
        try (Response response = HttpClient.execute(HttpClient.METHOD_GET, url, Headers.of(headers), null)) {
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.header("Docker-Content-Digest"));
            }
            if (response.code() == 404) {
                return Optional.empty();
            }
            throw responseException(response);
        }
    }

    public List<String> tags(Reference reference, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(TAGS, reference.getEndpoint(), reference.getName());
        try (Response response = HttpClient.execute(HttpClient.METHOD_GET, url, Headers.of(headers), null)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
            if (response.body() != null) {
                String body = response.body().string();
                return JsonUtil.fromJson(body, TagsResp.class).getTags();
            }
        }
        return Collections.emptyList();
    }

    public Optional<ManifestHttp> getManifest(Reference reference, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        headers.put(HttpHeaders.ACCEPT, ImageMediaType.MANIFEST_V2.toString());
        String url = String.format(MANIFEST, reference.getEndpoint(), reference.getName(),
                Optional.ofNullable(reference.getDigest()).orElse(reference.getTag()));
        try (Response response = HttpClient.execute(HttpClient.METHOD_GET, url, Headers.of(headers), null)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
            if (response.body() != null) {
                return Optional.ofNullable(JsonUtil.fromJson(response.body().string(), ManifestHttp.class));
            }
        }
        return Optional.empty();
    }

    public InputStream getBlob(Reference reference, String layerDigest, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(BLOB, reference.getEndpoint(), reference.getName(), layerDigest);
        Response response = HttpClient.execute(HttpClient.METHOD_GET, url, Headers.of(headers), null);
        if (response.isSuccessful()) {
            if (response.body() != null) {
                return response.body().byteStream();
            }
        }
        throw responseException(response);
    }

    public boolean isBlobExists(Reference reference, String layerDigest, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(BLOB, reference.getEndpoint(), reference.getName(), layerDigest);
        try (Response response = HttpClient.execute(HttpClient.METHOD_HEAD, url, Headers.of(headers), null)) {
            return response.isSuccessful();
        }
    }

    public String startPush(Reference reference, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(BLOB_UPLOAD, reference.getEndpoint(), reference.getName());
        try (Response response = HttpClient.execute(HttpClient.METHOD_POST, url, Headers.of(headers),
                RequestBody.create("", MediaType.parse(ImageMediaType.LAYER.toString())))) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
            return HttpClient.getLocation(response, url);
        }
    }

    public Optional<String> mountBlob(Reference dstReference, String digest, String srcName, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String mountUrl = String.format(BLOB_UPLOAD, dstReference.getEndpoint(), dstReference.getName())
                + String.format("?mount=%s&from=%s", digest, srcName);
        try (Response response = HttpClient.execute(HttpClient.METHOD_POST, mountUrl, Headers.of(headers),
                RequestBody.create(new byte[0], MediaType.parse(ImageMediaType.MANIFEST_V2.toString())))) {
            if (response.code() == 201) {
                return Optional.empty();
            }
            if (response.code() == 202) {
                return Optional.of(HttpClient.getLocation(response, dstReference.getEndpoint()));
            }
            throw responseException(response);
        }
    }

    public void uploadBlob(String url, String digest, InputStream inputStream, Long length, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String appendQuery = new URL(url).getQuery() == null ? "?" : "&";
        appendQuery += "digest=" + digest;
        RequestBody body = new InputStreamRequestBody(inputStream, length, MediaType.parse("application/octet-stream"));
        try (Response response = HttpClient.execute(HttpClient.METHOD_PUT, url + appendQuery, Headers.of(headers), body)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
        }
    }

    public void deleteLayer(Reference reference, String digest, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(BLOB, reference.getEndpoint(), reference.getName(), digest);
        try (Response response = HttpClient.execute(HttpClient.METHOD_DELETE, url, Headers.of(headers), null)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
        }
    }

    public void deleteManifest(Reference reference, String digest, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(MANIFEST, reference.getEndpoint(), reference.getName(), digest);
        try (Response response = HttpClient.execute(HttpClient.METHOD_DELETE, url, Headers.of(headers), null)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
        }
    }

    public void uploadManifest(Reference reference, ManifestHttp content, ImageMediaType contentType, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        RequestBody body = RequestBody.create(JsonUtil.toJson(content), MediaType.parse(contentType.toString()));
        String url = String.format(MANIFEST, reference.getEndpoint(), reference.getName(), reference.getTag());
        try (Response response = HttpClient.execute(HttpClient.METHOD_PUT, url, Headers.of(headers), body)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
        }
    }

    public CatalogResp catalog(Reference reference, Integer count, String last, String token) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(token).ifPresent(t -> headers.put(HttpHeaders.AUTHORIZATION, t));
        String url = String.format(CATALOG, reference.getEndpoint(),
                Optional.ofNullable(count).map(Object::toString).orElse(""), Optional.ofNullable(last).orElse(""));
        try (Response response = HttpClient.execute(HttpClient.METHOD_GET, url, Headers.of(headers), null)) {
            if (!response.isSuccessful()) {
                throw responseException(response);
            }
            if (response.body() == null) {
                throw new HttpCodeErrorException("status code:" + response.code());
            }
            return JsonUtil.fromJson(response.body().string(), CatalogResp.class);
        }

    }

    private IOException responseException(Response response) throws IOException {
        if (response.body() == null) {
            throw new HttpCodeErrorException("status code:" + response.code());
        }
        ErrorResponse error = JsonUtil.fromJson(response.body().string(), ErrorResponse.class);
        if (error == null || error.getErrors() == null || error.getErrors().isEmpty()) {
            throw new HttpCodeErrorException("status code:" + response.code());
        }
        throw new RegistryException(error.toString());
    }
}
