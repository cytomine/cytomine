package be.cytomine.common.repository.http;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.Role;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

import static be.cytomine.common.repository.http.UserRoleHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface UserRoleHttpContract {

    String ROOT_PATH = "/user_roles";

    @GetExchange
    Page<UserRoleResponse> list(Pageable pageable);

    @GetExchange("/{id}")
    Optional<UserRoleResponse> get(@PathVariable long id);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId, @Valid @RequestBody CreateUserRole payload);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id, @RequestParam long userId,
        @RequestBody UpdateUserRole payload);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/by-user/{userId}")
    Page<UserRoleResponse> listByUserId(@PathVariable long userId, Pageable pageable);

    @GetExchange("/by-user/{userId}/by-role/{roleId}")
    Optional<UserRoleResponse> getByUserIdAndRoleId(@PathVariable long userId, @PathVariable long roleId);

    @PutExchange("/define/{targetUserId}/role/{targetRole}")
    Set<UserRoleResponse> define(@RequestParam long userId, @PathVariable long targetUserId,
        @PathVariable Role targetRole);
}
