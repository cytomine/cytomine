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

import be.cytomine.common.repository.http.RoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.role.payload.CreateRole;
import be.cytomine.common.repository.model.role.payload.UpdateRole;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RoleController {

    private static final String UNABLE_TO_FIND_ROLE = "Unable to find role with id: %s";

    private final CurrentUserService currentUserService;
    private final RoleHttpContract roleHttpContract;
    private final PageMapper pageMapper;

    @GetMapping("/role.json")
    public CollectionResponse<RoleResponse> list(Pageable pageable) {
        log.debug("GET /role.json");
        return pageMapper.toCollectionResponse(roleHttpContract.list(pageable));
    }

    @GetMapping("/role/{id}.json")
    public RoleResponse show(@PathVariable long id) {
        log.debug("GET /role/{}.json", id);
        return roleHttpContract.get(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ROLE, id)));
    }

    @PostMapping("/role.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateRole payload) {
        log.debug("POST /role.json - {}", payload);
        long userId = currentUserService.getCurrentUser().id();
        return roleHttpContract.create(userId, payload);
    }

    @PutMapping("/role/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateRole payload) {
        log.debug("PUT /role/{}.json - {}", id, payload);
        long userId = currentUserService.getCurrentUser().id();
        return roleHttpContract.update(id, userId, payload)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ROLE, id)));
    }

    @DeleteMapping("/role/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /role/{}.json", id);
        long userId = currentUserService.getCurrentUser().id();
        return roleHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_ROLE, id)));
    }
}
