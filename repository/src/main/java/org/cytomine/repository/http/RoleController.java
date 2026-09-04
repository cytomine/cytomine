package org.cytomine.repository.http;


import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.RoleMapper;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.service.RoleCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.RoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.role.payload.CreateRole;
import be.cytomine.common.repository.model.role.payload.UpdateRole;

import static be.cytomine.common.repository.http.RoleHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class RoleController implements RoleHttpContract {
    private final RoleCommandService service;
    private final RoleRepository repository;
    private final RoleMapper mapper;

    @Override
    public Page<RoleResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::mapToRoleResponse);
    }

    @Override
    public Optional<RoleResponse> get(long id) {
        return repository.findByIdAndDeletedNull(id).map(mapper::mapToRoleResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateRole payload) {
        return service.create(userId, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateRole payload) {
        return service.update(userId, id, payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(MICROS));
    }
}
