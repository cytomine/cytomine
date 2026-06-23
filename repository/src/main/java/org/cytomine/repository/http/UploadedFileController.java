package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UploadedFileMapper;
import org.cytomine.repository.persistence.UploadedFileRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.UploadedFileCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UploadedFileHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;

import static be.cytomine.common.repository.http.UploadedFileHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RestController
@RequestMapping(ROOT_PATH)
@RequiredArgsConstructor
public class UploadedFileController implements UploadedFileHttpContract {
    private final ACLService aclService;
    private final UploadedFileCommandService service;
    private final UploadedFileMapper mapper;
    private final UploadedFileRepository repository;

    @Override
    public Optional<UploadedFileResponse> get(long id, long userId) {
        return repository.findByIdAndDeletedNull(id)
            .filter(entity -> aclService.canReadStorage(userId, entity.getStorageId()))
            .map(mapper::mapToUploadedFileResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateUploadedFile payload) {
        return service.create(userId, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUploadedFile payload) {
        return service.update(userId, id, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Page<UploadedFileResponse> getAll(long userId, Pageable pageable) {
        return service.getAll(userId, pageable);
    }
}
