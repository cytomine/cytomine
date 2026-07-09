package be.cytomine.common.repository.http;

import java.util.Optional;

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

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.role.payload.CreateRole;
import be.cytomine.common.repository.model.role.payload.UpdateRole;

import static be.cytomine.common.repository.http.RoleHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface RoleHttpContract {
    String ROOT_PATH = "/roles";

    @GetExchange
    Page<RoleResponse> list(Pageable pageable);

    @GetExchange("/{id}")
    Optional<RoleResponse> get(@PathVariable long id);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId, @Valid @RequestBody CreateRole payload);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateRole payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
