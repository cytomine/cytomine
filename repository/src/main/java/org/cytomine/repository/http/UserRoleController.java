package org.cytomine.repository.http;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UserRoleHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

import static be.cytomine.common.repository.http.UserRoleHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class UserRoleController implements UserRoleHttpContract {
    @Override
    public Page<UserRoleResponse> list(Pageable pageable) {
        return null;
    }

    @Override
    public Optional<UserRoleResponse> get(long id) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateUserRole payload) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUserRole payload) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return Optional.empty();
    }
}
