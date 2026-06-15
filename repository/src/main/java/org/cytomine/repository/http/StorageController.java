package org.cytomine.repository.http;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;

import static be.cytomine.common.repository.http.StorageHttpContract.ROOT_PATH;

@RestController
@RequestMapping(ROOT_PATH)
@RequiredArgsConstructor
public class StorageController implements StorageHttpContract {
    @Override
    public Optional<StorageResponse> get(long id, long userId) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateStorage payload) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateStorage payload) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return Optional.empty();
    }
}
