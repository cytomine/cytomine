package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UserRoleMapper;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.persistence.UserRoleRepository;
import org.cytomine.repository.persistence.entity.RoleEntity;
import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.cytomine.repository.service.UserRoleCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

import static be.cytomine.common.repository.http.UserRoleHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class UserRoleController implements UserRoleHttpContract {

    private static final List<String> ROLE_ORDER = List.of("ROLE_GUEST", "ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

    private final UserRoleCommandService service;
    private final UserRoleRepository repository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper mapper;

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
    public Optional<UserRoleResponse> getHighestByUserId(long userId) {
        return repository.findHighestBySecUserId(userId).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public Optional<UserRoleResponse> getByUserIdAndRoleId(long userId, long roleId) {
        return repository.findBySecUserIdAndSecRoleIdAndDeletedNull(userId, roleId).map(mapper::mapToUserRoleResponse);
    }

    @Override
    public void define(long userId, long roleId, long requestingUserId) {
        RoleEntity targetRole = roleRepository.findByIdAndDeletedNull(roleId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found: " + roleId));

        int targetLevel = ROLE_ORDER.indexOf(targetRole.getAuthority());
        LocalDateTime now = LocalDateTime.now().truncatedTo(MICROS);

        for (int i = 0; i < ROLE_ORDER.size(); i++) {
            String authority = ROLE_ORDER.get(i);
            Optional<RoleEntity> maybeRole = roleRepository.findByAuthorityAndDeletedNull(authority);

            Optional<UserRoleEntity> existing = maybeRole.flatMap(
                existingUserRole -> repository.findBySecUserIdAndSecRoleIdAndDeletedNull(userId,
                    existingUserRole.getId()));

            if (i <= targetLevel && existing.isEmpty()) {
                maybeRole.map(role -> service.create(requestingUserId, new CreateUserRole(userId, role.getId()), now));
            } else if (i > targetLevel && existing.isPresent()) {
                service.delete(requestingUserId, existing.get().getId(), now);
            }
        }
    }
}
