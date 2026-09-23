package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UploadedFileMapper;
import org.cytomine.repository.persistence.UploadedFileRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    @Override
    public Optional<UploadedFileResponse> get(long id) {
        return repository.findByIdAndDeletedNull(id)
            .filter(entity -> aclService.canReadStorage(currentUserService.getCurrentUserId(),
                entity.getStorageId()))
            .map(mapper::mapToUploadedFileResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(CreateUploadedFile payload) {
        return service.create(currentUserService.getCurrentUserId(), payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, UpdateUploadedFile payload) {
        return service.update(currentUserService.getCurrentUserId(), id, payload,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id) {
        return service.delete(currentUserService.getCurrentUserId(), id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Page<UploadedFileResponse> getAll(List<Long> uploadedFileIds, Pageable pageable) {
        return service.getAll(currentUserService.getCurrentUserId(), uploadedFileIds, pageable);
    }
}
