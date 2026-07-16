package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UserMapper;
import org.cytomine.repository.persistence.UserRepository;
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

    @Override
    public Optional<UserResponse> get(long id, long userId) {
        return repository.findByIdAndDeletedNull(id).map(mapper::mapToUserResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateUser createUser) {
        return service.create(userId, createUser, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUser updateUser) {
        return service.update(userId, id, updateUser, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<UserResponse> search(String username) {
        return repository.findByUsernameLikeIgnoreCase(username).map(mapper::mapToUserResponse);
    }

    @Override
    public Page<UserResponse> findByIdsIn(Set<Long> ids, Pageable pageable) {
        return repository.findByIdsIn(ids, pageable);
    }
}
