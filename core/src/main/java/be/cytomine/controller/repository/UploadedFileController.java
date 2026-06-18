package be.cytomine.controller.repository;

import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import be.cytomine.common.repository.http.UploadedFileHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.middleware.ImageServerService;
import be.cytomine.service.middleware.ImageServerService.DownloadType;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class UploadedFileController {

    public static final String UNABLE_TO_FIND_UPLOADED_FILE = "Unable to find uploaded file with id: %s";

    private final CurrentUserService currentUserService;
    private final ImageServerService imageServerService;
    private final UploadedFileHttpContract uploadedFileHttpContract;

    @PostMapping("/uploadedfile.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateUploadedFile payload) {
        log.debug("POST /uploadedfile.json - {}", payload);
        long userId = currentUserService.getCurrentUser().getId();
        return uploadedFileHttpContract.create(userId, payload);
    }

    @GetMapping("/uploadedfile/{id}.json")
    public UploadedFileResponse show(@PathVariable Long id) {
        log.debug("GET /uploadedFile/{}.json", id);
        long userId = currentUserService.getCurrentUser().getId();
        return uploadedFileHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_UPLOADED_FILE, id)));
    }

    @PutMapping("/uploadedfile/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateUploadedFile payload) {
        log.debug("PUT /uploadedfile/{}.json - {}", id, payload);
        long userId = currentUserService.getCurrentUser().getId();
        return uploadedFileHttpContract.update(id, userId, payload)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_UPLOADED_FILE, id)));
    }

    @DeleteMapping("/uploadedfile/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /uploadedfile/{}.json", id);
        long userId = currentUserService.getCurrentUser().getId();
        return uploadedFileHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_UPLOADED_FILE, id)));
    }

    @GetMapping("/uploadedfile/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable Long id) throws IOException {
        log.debug("GET /uploadedfile/{}/download", id);
        long userId = currentUserService.getCurrentUser().getId();

        UploadedFileResponse uploadedFile = uploadedFileHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_UPLOADED_FILE, id)));

        StreamingResponseBody stream = outputStream -> imageServerService.streamDownload(
            DownloadType.FILE,
            uploadedFile.path(),
            uploadedFile.originalFilename(),
            outputStream
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", uploadedFile.originalFilename());

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(stream);
    }
}
