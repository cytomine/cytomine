package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    public static final String UNABLE_TO_FIND_USER = "Unable to find user with id: %s";

    private final UserHttpContract userHttpContract;
    private final CurrentUserService currentUserService;

    @GetMapping("/user/{id}.json")
    public UserResponse show(@PathVariable long id) {
        log.debug("REST request to get User : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return userHttpContract.get(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_USER, id)));
    }

    @PostMapping("/user.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateUser createUser) {
        log.debug("REST request to save User");
        long userId = currentUserService.getCurrentUser().getId();
        return userHttpContract.create(userId, createUser);
    }

    @PutMapping("/user/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateUser updateUser) {
        log.debug("REST request to update User : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return userHttpContract.update(id, userId, updateUser)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_USER, id)));
    }

    @DeleteMapping("/user/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete User : {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return userHttpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_USER, id)));
    }
}
