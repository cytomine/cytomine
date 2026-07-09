package org.cytomine.repository.http;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.cytomine.repository.mapper.UserRoleMapper;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.persistence.UserRoleRepository;
import org.cytomine.repository.persistence.entity.RoleEntity;
import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.cytomine.repository.service.RoleHierarchy;
import org.cytomine.repository.service.UserRoleCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.Role;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

import static be.cytomine.common.repository.http.UserRoleHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class UserRoleController implements UserRoleHttpContract {

    private final List<String> ROLE_ORDER = List.of("ROLE_GUEST", "ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
    private final UserRoleCommandService service;
    private final UserRoleRepository repository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper mapper;

    private final RoleHierarchy roleHierarchy;

    @Override
    public Page<UserRoleResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<UserRoleResponse> get(long id) {
        return repository.findByIdAndDeletedNull(id).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateUserRole payload) {
        return service.create(userId, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUserRole payload) {
        return service.update(userId, id, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(MICROS));
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
    public void define(long userId, Role role, long requestingUserId) {

        LocalDateTime now = LocalDateTime.now().truncatedTo(MICROS);
        Timestamp now2 = Timestamp.from(Instant.now());

        Set<String> targetRoles = roleHierarchy.getRolesUpTo(role).stream().map(Role::name).collect(Collectors.toSet());
        Set<Long> targetRoleEntities =
            roleRepository.findAllByAuthorityAndDeletedNull(userId, targetRoles).stream().map(RoleEntity::getId)
                .collect(
                    Collectors.toSet());

        Set<UserRoleEntity> userRoleEntities = repository.findAllBySecUserId(userId);

        Set<UserRoleEntity> rolesToRemove =
            userRoleEntities.stream()
                .filter(r-> r.getDeleted() == null)
                .filter(r-> !targetRoleEntities.contains(r.getSecRoleId()))
                .map(userRoleEntity -> mapper.delete(userRoleEntity, now2))
                .collect(
                Collectors.toSet());

        Set<UserRoleEntity> rolesToAdd =
            targetRoleEntities.stream().filter(targetRoleEntity -> !userRoleEntities.contains(targetRoleEntity))
                .collect(
                    Collectors.toSet());

        repository.save();


    }
}
