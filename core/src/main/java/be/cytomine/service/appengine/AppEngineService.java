package be.cytomine.service.appengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppEngineService {

    private final RestTemplate restTemplate;
    @Value("${application.appEngine.apiUrl}")
    private String apiUrl;
    @Value("${application.appEngine.apiBasePath}")
    private String apiBasePath;

    private String buildFullUrl(String uri) {
        return apiUrl + apiBasePath + uri;
    }

    public String get(String uri) {
        return restTemplate.exchange(buildFullUrl(uri), HttpMethod.GET, null, String.class).getBody();
    }

    public File getStreamedFile(String uri) {
        Path filePath = Paths.get("downloaded_" + System.currentTimeMillis() + ".tmp");
        File targetFile = filePath.toFile();

        restTemplate.execute(buildFullUrl(uri), HttpMethod.GET, null, response -> {
            try (InputStream in = response.getBody(); OutputStream out =
                new FileOutputStream(targetFile)) {
                StreamUtils.copy(in, out);
                return null;
            }
        });

        return targetFile;
    }

    public <B> String sendWithBody(HttpMethod method, String uri, B body, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        HttpEntity<B> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> result = restTemplate.exchange(buildFullUrl(uri), method,
                request, String.class);
            return result.getBody();
        } catch (RestClientException e) {
            throw new RestClientException("Internal error");
        }
    }

    public <B> String post(String uri, B body, MediaType contentType) {
        return sendWithBody(HttpMethod.POST, uri, body, contentType);
    }

    public <B> String put(String uri, B body, MediaType contentType) {
        return sendWithBody(HttpMethod.PUT, uri, body, contentType);
    }

    public <B> String putWithParams(String uri, B body, MediaType contentType, Map<String,
        String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        String finalUrl = builder.toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpEntity<B> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(buildFullUrl(finalUrl), HttpMethod.PUT, requestEntity,
            String.class).getBody();
    }
}
