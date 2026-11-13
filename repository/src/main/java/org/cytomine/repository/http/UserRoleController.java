package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.repository.http.UserRoleHttpContract;
import org.cytomine.common.repository.model.Role;
import org.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import org.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import org.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import org.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;
import org.cytomine.repository.mapper.UserRoleMapper;
import org.cytomine.repository.persistence.UserRoleRepository;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.UserRoleCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.cytomine.common.repository.http.UserRoleHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class UserRoleController implements UserRoleHttpContract {

    private final UserRoleCommandService service;
    private final UserRoleRepository repository;
    private final UserRoleMapper mapper;
    private final CurrentUserService currentUserService;

    @Override
    public Page<UserRoleResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<UserRoleResponse> get(long id) {
        return repository.findByIdAndDeletedNull(id).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(CreateUserRole payload) {
        return service.create(currentUserService.getCurrentUserId(), payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, UpdateUserRole payload) {
        return service.update(currentUserService.getCurrentUserId(), id, payload,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id) {
        return service.delete(currentUserService.getCurrentUserId(), id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Page<UserRoleResponse> listByUserId(long userId, Pageable pageable) {
        return repository.findAllBySecUserIdAndDeletedNull(userId, pageable).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<UserRoleResponse> getByUserIdAndRoleId(long userId, long roleId) {
        return repository.findBySecUserIdAndSecRoleIdAndDeletedNull(userId, roleId).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Set<UserRoleResponse> define(long targetUserId, Role targetRole) {
        return service.define(currentUserService.getCurrentUserId(), targetUserId, targetRole);
    }
}
