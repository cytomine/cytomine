package org.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import org.cytomine.common.repository.model.command.payload.response.StorageResponse;
import org.cytomine.common.repository.model.storage.payload.CreateStorage;
import org.cytomine.common.repository.model.storage.payload.UpdateStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(StorageHttpContract.ROOT_PATH)
public interface StorageHttpContract {
    String ROOT_PATH = "/storages";

    @GetExchange("/{id}")
    Optional<StorageResponse> get(@PathVariable long id);

    @PostExchange
    Optional<HttpCommandResponse> create(
        @Valid @RequestBody CreateStorage payload
    );

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestBody UpdateStorage payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id);

    @GetExchange("/all")
    Page<StorageResponse> getAll(Pageable pageable);
}
