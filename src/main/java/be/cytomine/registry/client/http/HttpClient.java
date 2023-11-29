package io.github.ya_b.registry.client.http;

import io.github.ya_b.registry.client.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class HttpClient {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    public static final String URL_PREFIX = "(http|https)://";
    public static final String URL_SUFFIX = "/.*";


    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(chain -> {
                Request request = chain.request();
                String requestId = UUID.randomUUID().toString();
                log.debug(String.format("requestId: %s, %s : %s", requestId, request.method(), request.url()));
                Response response = chain.proceed(request);
                log.debug(String.format("requestId: %s, resp: %s", requestId, response.code()));
                return response;
            })
            .build();

    public static Response execute(String method, String url, Headers headers, RequestBody requestBody) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .method(method, requestBody)
                .url(Objects.requireNonNull(url));
        if (headers != null) {
            headers.forEach(p -> requestBuilder.addHeader(p.getFirst(), p.getSecond()));
        }
        return okHttpClient.newCall(requestBuilder.build()).execute();
    }

    public static String getLocation(Response response, String url) {
        String str = response.header(HttpHeaders.LOCATION);
        if (str == null || str.isEmpty()) {
            throw new NullPointerException("location not found in headers");
        }
        if (str.startsWith(Constants.SCHEMA_HTTP)) {
            return str;
        }
        return String.format("%s//%s%s", url.replaceAll(URL_SUFFIX, ""), domainFrom(url), str);
    }

    public static String domainFrom(String url) {
        return url.replaceAll(URL_PREFIX, "").replaceAll(URL_SUFFIX, "");
    }
}
