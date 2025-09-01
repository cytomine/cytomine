package be.cytomine.appengine.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import be.cytomine.appengine.dto.appstore.InstallRequest;


@Service
public class AppStoreService {


    public File downloadTask(InstallRequest request)
        throws IOException {
        log.info("Download Task: downloading ... {}:{}",
            request.getAppStoreNamespace(),
            request.getAppStoreVersion());
        RestTemplate restTemplate = new RestTemplate();
        Path tempPath = Path.of("bundle-" + UUID.randomUUID() + ".zip");
        restTemplate.execute(
                request.getAppStorescheme()
                + "://"
                + request.getAppStoreHost() + "/api/v1/tasks/{namespace}/{version}/bundle.zip",
                org.springframework.http.HttpMethod.GET,
                null,
                clientHttpResponse -> {
                    Files.copy(clientHttpResponse.getBody(), tempPath);
                    return null;
                },
                request.getAppNamespace(),
                request.getAppVersion()
        );
        log.info("Download Task: downloaded");
        return tempPath.toFile();
    }
}
