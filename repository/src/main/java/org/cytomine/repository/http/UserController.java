package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UserMapper;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.UserCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;

import static be.cytomine.common.repository.http.UserHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RestController
@RequestMapping(ROOT_PATH)
@RequiredArgsConstructor
public class UserController implements UserHttpContract {
    private final UserCommandService service;
    private final UserMapper mapper;
    private final UserRepository repository;
    private final CurrentUserService currentUserService;

    @Override
    public Optional<UserResponse> get(long id) {
        return repository.findByIdAndDeletedNull(id).map(mapper::mapToUserResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(CreateUser createUser) {
        return service.create(currentUserService.getCurrentUserId(), createUser,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> selfRegister(CreateUser createUser) {
        return service.createSelf(createUser, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, UpdateUser updateUser) {
        return service.update(currentUserService.getCurrentUserId(), id, updateUser,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id) {
        return service.delete(currentUserService.getCurrentUserId(), id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<UserResponse> search(String username) {
        return repository.findByUsername(username).map(mapper::mapToUserResponse);
    }

    @Override
    public Page<UserResponse> findByIdsIn(Set<Long> ids, Pageable pageable) {
        return repository.findByIdIn(ids, pageable);
    }
}
