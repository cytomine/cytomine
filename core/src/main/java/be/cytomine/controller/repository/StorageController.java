package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class StorageController {

    public static final String UNABLE_TO_FIND_STORAGE = "Unable to find storage with id: %s";

    private final CurrentUserService currentUserService;
    private final PageMapper pageMapper;
    private final StorageHttpContract storageHttpContract;

    @GetMapping("/storage.json")
    public CollectionResponse<StorageResponse> getAllReadableByUser(Pageable pageable) {
        log.debug("GET /storage.json");
        long userId = currentUserService.getCurrentUser().id();
        return pageMapper.toCollectionResponse(storageHttpContract.getAll(userId, pageable));
    }

    @PostMapping("/storage.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateStorage payload) {
        log.debug("POST /storage.json - {}", payload);
        long userId = currentUserService.getCurrentUser().id();
        return storageHttpContract.create(userId, payload);
    }

    @GetMapping("/storage/{id}.json")
    public StorageResponse show(@PathVariable long id) {
        log.debug("GET /storage/{}.json", id);
        long userId = currentUserService.getCurrentUser().id();
        return storageHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_STORAGE, id)));
    }

    @PutMapping("/storage/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateStorage payload) {
        log.debug("PUT /storage/{}.json - {}", id, payload);
        long userId = currentUserService.getCurrentUser().id();
        return storageHttpContract.update(id, userId, payload)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_STORAGE, id)));
    }

    @DeleteMapping("/storage/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /storage/{}.json", id);
        long userId = currentUserService.getCurrentUser().id();
        return storageHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_STORAGE, id)));
    }
}
