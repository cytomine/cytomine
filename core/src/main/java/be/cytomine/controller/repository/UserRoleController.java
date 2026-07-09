package be.cytomine.controller.repository;

import java.util.Map;
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

import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class UserRoleController {

    private static final String UNABLE_TO_FIND_USER_ROLE = "Unable to find user role for user %s and role %s";

    private final CurrentUserService currentUserService;
    private final UserRoleHttpContract userRoleHttpContract;
    private final PageMapper pageMapper;

    @GetMapping("/user/{userId}/role.json")
    public CollectionResponse<UserRoleResponse> list(
        @PathVariable long userId,
        Pageable pageable
    ) {
        log.debug("GET /user/{}/role.json", userId);
        return pageMapper.toCollectionResponse(userRoleHttpContract.listByUserId(userId, pageable));
    }

    @GetMapping("/user/{userId}/role/{roleId}.json")
    public UserRoleResponse show(@PathVariable long userId, @PathVariable long roleId) {
        log.debug("GET /user/{}/role/{}.json", userId, roleId);
        return userRoleHttpContract.getByUserIdAndRoleId(userId, roleId)
            .orElseThrow(() -> new ResponseStatusException(
                NOT_FOUND, format(UNABLE_TO_FIND_USER_ROLE, userId, roleId)));
    }

    @PostMapping("/user/{userId}/role.json")
    public Optional<HttpCommandResponse> create(
        @PathVariable long userId,
        @RequestBody Map<String, Object> json
    ) {
        log.debug("POST /user/{}/role.json", userId);
        long roleId = ((Number) json.get("role")).longValue();
        long requestingUserId = currentUserService.getCurrentUser().getId();
        return userRoleHttpContract.create(requestingUserId, new CreateUserRole(userId, roleId));
    }

    @DeleteMapping("/user/{userId}/role/{roleId}.json")
    public HttpCommandResponse delete(@PathVariable long userId, @PathVariable long roleId) {
        log.debug("DELETE /user/{}/role/{}.json", userId, roleId);
        long requestingUserId = currentUserService.getCurrentUser().getId();
        UserRoleResponse userRole = userRoleHttpContract.getByUserIdAndRoleId(userId, roleId)
            .orElseThrow(() -> new ResponseStatusException(
                NOT_FOUND, format(UNABLE_TO_FIND_USER_ROLE, userId, roleId)));
        return userRoleHttpContract.delete(userRole.id(), requestingUserId)
            .orElseThrow(() -> new ResponseStatusException(
                NOT_FOUND, format(UNABLE_TO_FIND_USER_ROLE, userId, roleId)));
    }

    @PutMapping("/user/{userId}/role/{roleId}/define.json")
    public CollectionResponse<UserRoleResponse> define(
        @PathVariable long userId,
        @PathVariable long roleId,
        Pageable pageable
    ) {
        log.debug("PUT /user/{}/role/{}/define.json", userId, roleId);
        long requestingUserId = currentUserService.getCurrentUser().getId();
        userRoleHttpContract.define(userId, roleId, requestingUserId);
        return pageMapper.toCollectionResponse(userRoleHttpContract.listByUserId(userId, pageable));
    }
}
