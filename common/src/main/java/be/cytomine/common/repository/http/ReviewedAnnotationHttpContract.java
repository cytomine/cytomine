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
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;

import static be.cytomine.common.repository.http.ReviewedAnnotationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface ReviewedAnnotationHttpContract {
    String ROOT_PATH = "/reviewed_annotations";

    @GetExchange("/{id}")
    Optional<ReviewedAnnotationResponse> findById(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/term/{termId}/count")
    long countByTerm(@PathVariable long termId, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId,
                                         @Valid @RequestBody CreateReviewedAnnotation createReviewedAnnotation);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
                                         @RequestParam long userId,
                                         @RequestBody UpdateReviewedAnnotation updateReviewedAnnotation);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);
}
