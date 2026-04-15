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

import be.cytomine.common.repository.model.annotationterm.payload.CreateAnnotationTerm;
import be.cytomine.common.repository.model.command.payload.response.AnnotationTermResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

import static be.cytomine.common.repository.http.AnnotationTermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface AnnotationTermHttpContract {
    String ROOT_PATH = "/annotation_terms";

    @GetExchange("/{id}")
    Optional<AnnotationTermResponse> findById(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/user_annotation/{annotationId}")
    List<AnnotationTermResponse> findByUserAnnotation(@PathVariable long annotationId, @RequestParam long userId);

    @GetExchange("/user_annotation/{annotationId}/user/{termUserId}")
    List<AnnotationTermResponse> findByUserAnnotationAndUser(@PathVariable long annotationId,
                                                             @PathVariable long termUserId,
                                                             @RequestParam long userId);

    @GetExchange("/project/{projectId}")
    List<AnnotationTermResponse> findByProject(@PathVariable long projectId, @RequestParam long userId);

    @GetExchange("/term/{termId}/count")
    long countByTerm(@PathVariable long termId, @RequestParam long userId);

    @GetExchange("/find")
    Optional<AnnotationTermResponse> findByAnnotationAndTermAndUser(@RequestParam long annotationId,
                                                                    @RequestParam long termId,
                                                                    @RequestParam long termUserId,
                                                                    @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId,
                                         @Valid @RequestBody CreateAnnotationTerm createAnnotationTerm);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
