package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;

import static be.cytomine.common.repository.http.UploadedFileHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface UploadedFileHttpContract {
    String ROOT_PATH = "/uploaded-files";

    @GetExchange("/{id}")
    Optional<UploadedFileResponse> get(@PathVariable long id, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(
        @RequestParam long userId,
        @Valid @RequestBody CreateUploadedFile createPayload
    );

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateUploadedFile payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
