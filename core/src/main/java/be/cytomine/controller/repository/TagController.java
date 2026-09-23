package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.TagHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.common.repository.model.tag.payload.CreateTag;
import be.cytomine.common.repository.model.tag.payload.UpdateTag;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TagController {
    public static final String UNABLE_TO_FIND_TAG = "Unable to find tag with id: %s";

    private final PageMapper pageMapper;
    private final TagHttpContract tagHttpContract;

    @GetMapping("/tag.json")
    public CollectionResponse<TagResponse> list(
        @SortDefault(sort = "created", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("GET /tag.json");
        return pageMapper.toCollectionResponse(tagHttpContract.list(pageable));
    }

    @GetMapping("/tag/{id}.json")
    public TagResponse read(@PathVariable long id) {
        log.debug("GET /tag/{}.json", id);
        return tagHttpContract.read(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TAG, id)));
    }

    @PostMapping("/tag.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateTag payload) {
        log.debug("POST /tag.json - {}", payload);
        return tagHttpContract.create(payload);
    }

    @PutMapping("/tag/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateTag payload) {
        log.debug("PUT /tag/{}.json - {}", id, payload);
        return tagHttpContract.update(id, payload)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TAG, id)));
    }

    @DeleteMapping("/tag/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /tag/{}.json", id);
        return tagHttpContract.delete(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TAG, id)));
    }
}
