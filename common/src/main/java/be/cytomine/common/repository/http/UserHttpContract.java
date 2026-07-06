package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;

@HttpExchange(UserHttpContract.ROOT_PATH)
public interface UserHttpContract {

    String ROOT_PATH = "/users";

    @GetExchange("/{id}")
    Optional<UserResponse> get(@PathVariable long id, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId, @Valid @RequestBody CreateUser createUser);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
        @RequestParam long userId,
        @RequestBody UpdateUser updateUser);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);

}
