package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.TagRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.TagCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TagHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.common.repository.model.tag.payload.CreateTag;
import be.cytomine.common.repository.model.tag.payload.UpdateTag;

@RequiredArgsConstructor
@RestController
@RequestMapping(TagHttpContract.ROOT_PATH)
public class TagController implements TagHttpContract {
    private final ACLService aclService;
    private final TagCommandService service;
    private final TagRepository repository;

    @Override
    public Optional<HttpCommandResponse> create(@RequestParam long userId, @RequestBody CreateTag payload) {
        return service.create(userId, payload, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    public Optional<TagResponse> read(@PathVariable long id, @RequestParam long userId) {
        return repository.findByIdAndDeletedNull(id).map(service::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateTag payload
    ) {
        return service.update(userId, id, payload, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    @Override
    public Page<TagResponse> list(@RequestParam long userId, Pageable pageable) {
        if (aclService.isAdmin(userId)) {
            return repository.findAllByDeletedNull(pageable).map(service::mapToResponse);
        }
        return repository.findAllReadableByUser(userId, pageable).map(service::mapToResponse);
    }
}
