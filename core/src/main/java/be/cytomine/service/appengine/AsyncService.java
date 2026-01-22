package be.cytomine.service.appengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


import be.cytomine.config.security.ApiKeyFilter;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.security.User;
import be.cytomine.dto.appengine.task.TaskRunValue;
import be.cytomine.service.image.AbstractImageService;
import be.cytomine.service.image.server.StorageService;
import be.cytomine.service.middleware.ImageServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final AbstractImageService abstractImageService;

    private final AppEngineService appEngineService;

    private final RestTemplate restTemplate;

    private final StorageService storageService;

    private final ImageServerService imageServerService;


    @Async
    public void launchImageAdditionJob(List<TaskRunValue> taskRunId, Long projectId,
                                       User currentUser) {
        // get all images and arrays of images
        List<TaskRunValue> outputs = taskRunId
            .stream()
            .filter(value ->
                value.getType().equalsIgnoreCase("IMAGE")
                    || (value.getType().equalsIgnoreCase("ARRAY")
                    && value.getSubType().equalsIgnoreCase("IMAGE"))
            )
            .toList();

        for (TaskRunValue output: outputs) {
            if (output.getType().equalsIgnoreCase("IMAGE")) {
                try {

                    handleImage(output, projectId, currentUser);
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            } else {
                handleImageArray(output);
            }
        }
    }

    private void handleImageArray(TaskRunValue output) {
        // TODO: implement storing image array
    }

    private void handleImage(TaskRunValue output, Long projectId, User currentUser)
        throws IOException, NoSuchAlgorithmException, InvalidKeyException
    {
        String originalFileName =
            output.getTaskRunId().toString() + "_" + output.getParameterName();
        if (abstractImageService.find(originalFileName).isPresent()) {
            return;
        }
        // download the image from app-engine
        File tempFile = Files.createTempFile("image_", ".tmp").toFile();
        try (FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {
            getTaskRunIOParameter(projectId, output.getTaskRunId(), output.getParameterName(), "output", tempFileOutputStream);
        }
        // signature
        String signatureDate = Instant.now().toString();
        String signature = ApiKeyFilter.generateKeys("POST","","",
            signatureDate,currentUser);
        String authorizationHeader = "CYTOMINE " + currentUser.getPublicKey() + ":" + signature;
        String contentTypeFull = null;

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("authorization", authorizationHeader);
        headers.set("dateFull", signatureDate);
        headers.set("content-type-full", contentTypeFull);

        // Prepare a multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files[]", new FileSystemResource(tempFile){
            @Override
            public String getFilename() {
                return originalFileName; // Use the desired name here
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        Storage userStorage = storageService.list(currentUser).stream().findFirst().orElseThrow();
        String queryString = "?idStorage=" + userStorage.getId() + "&idProject=" + projectId;
        // Send the request
        String uploadUrl = imageServerService.internalImageServerURL() + "/upload";
        ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl + queryString, requestEntity, String.class);

        // Clean up temp file
        tempFile.delete();
    }

    public void getTaskRunIOParameter(Long projectId, UUID taskRunId, String parameterName, String type, OutputStream outputStream) {
        appEngineService.getStreamedFile("task-runs/" + taskRunId + "/" + type + "/" + parameterName, outputStream);
    }
}
