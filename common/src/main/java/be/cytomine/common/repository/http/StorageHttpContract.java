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
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;

import static be.cytomine.common.repository.http.StorageHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface StorageHttpContract {
    String ROOT_PATH = "/storage";

    @GetExchange("/{id}")
    Optional<StorageResponse> get(@PathVariable long id, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(
        @RequestParam long userId,
        @Valid @RequestBody CreateStorage payload
    );

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateStorage payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
