package org.cytomine.repository.http;

import java.time.Instant;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.StorageMapper;
import org.cytomine.repository.persistence.StorageRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.StorageCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;

import static be.cytomine.common.repository.http.StorageHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RestController
@RequestMapping(ROOT_PATH)
@RequiredArgsConstructor
public class StorageController implements StorageHttpContract {
    private final ACLService aclService;
    private final StorageCommandService service;
    private final StorageMapper mapper;
    private final StorageRepository repository;

    @Override
    public Page<StorageResponse> getAll(long userId, Pageable pageable) {
        if (aclService.isAdmin(userId)) {
            return repository.findAllByDeletedNull(pageable).map(mapper::mapToStorageResponse);
        }
        return repository.findAllReadableByUser(userId, pageable).map(mapper::mapToStorageResponse);
    }

    @Override
    public Optional<StorageResponse> get(long id, long userId) {
        return repository.findByIdAndDeletedNull(id)
            .filter(entity -> aclService.canReadStorage(userId, entity.getId()))
            .map(mapper::mapToStorageResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateStorage payload) {
        return service.create(userId, payload, Instant.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateStorage payload) {
        return service.update(userId, id, payload, Instant.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, Instant.now().truncatedTo(MICROS));
    }
}
