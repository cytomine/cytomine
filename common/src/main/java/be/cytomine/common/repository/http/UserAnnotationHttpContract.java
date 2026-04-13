package be.cytomine.common.repository.http;

import java.util.List;
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
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;
import be.cytomine.common.repository.model.userannotation.payload.CreateUserAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

import static be.cytomine.common.repository.http.UserAnnotationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface UserAnnotationHttpContract {
    String ROOT_PATH = "/user_annotations";

    @GetExchange("/{id}")
    Optional<UserAnnotationResponse> findById(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/user/{userAnnotationUserId}/image/{imageId}")
    List<UserAnnotationResponse> findAllByUserAndImage(@PathVariable long userAnnotationUserId,
                                                       @PathVariable long imageId,
                                                       @RequestParam long userId);

    @GetExchange("/count/project/{projectId}")
    long countByProject(@PathVariable long projectId, @RequestParam long userId,
                        @RequestParam(required = false) Long startDate,
                        @RequestParam(required = false) Long endDate);

    @GetExchange("/count/user/{userAnnotationUserId}")
    long countByUser(@PathVariable long userAnnotationUserId, @RequestParam long userId);

    @GetExchange("/count/user/{userAnnotationUserId}/project/{projectId}")
    long countByUserAndProject(@PathVariable long userAnnotationUserId,
                               @PathVariable long projectId,
                               @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId,
                                         @Valid @RequestBody CreateUserAnnotation createUserAnnotation);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
                                         @RequestParam long userId,
                                         @RequestBody UpdateUserAnnotation updateUserAnnotation);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
