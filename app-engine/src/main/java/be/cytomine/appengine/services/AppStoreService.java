package be.cytomine.appengine.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import be.cytomine.appengine.dto.responses.errors.AppEngineError;
import be.cytomine.appengine.dto.responses.errors.ErrorBuilder;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.AppStoreServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import be.cytomine.appengine.dto.appstore.InstallRequest;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.exceptions.BundleArchiveException;
import be.cytomine.appengine.exceptions.TaskServiceException;
import be.cytomine.appengine.exceptions.ValidationException;


@Slf4j
@AllArgsConstructor
@Service
public class AppStoreService {

    private final TaskService taskService;

    public Optional<TaskDescription> install(InstallRequest request)
            throws FileNotFoundException,
            TaskServiceException,
            ValidationException,
            BundleArchiveException,
            AppStoreServiceException {
        log.info("Install Task: installing ... {}:{}",
            request.getAppNamespace(),
            request.getAppVersion());
        File file = null;
        try {
            file = downloadTask(request);
        } catch (IOException | RestClientException e) {
            AppEngineError error = ErrorBuilder
                .build(ErrorCode.APPSTORE_DOWNLOAD_FAILED);
            throw new AppStoreServiceException(error);
        }
        assert file != null;
        Optional<TaskDescription> description = taskService.uploadTask(new FileInputStream(file));
        file.delete();
        log.info("Install Task: installed");
        return description;
    }


    public File downloadTask(InstallRequest request)
        throws IOException, RestClientException {
        log.info("Download Task: downloading ... {}:{}",
            request.getAppNamespace(),
            request.getAppVersion());
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
