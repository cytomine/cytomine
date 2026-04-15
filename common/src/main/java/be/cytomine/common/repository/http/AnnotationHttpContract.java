package be.cytomine.common.repository.http;

import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

import static be.cytomine.common.repository.http.AnnotationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface AnnotationHttpContract {
    String ROOT_PATH = "/annotations";

    @GetExchange("/{id}")
    Optional<ApplyCommandResponse> findById(@PathVariable long id, @RequestParam long userId);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
                                         @RequestParam long userId,
                                         @RequestBody UpdateUserAnnotation update);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
