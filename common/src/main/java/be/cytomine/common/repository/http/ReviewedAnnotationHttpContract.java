package be.cytomine.common.repository.http;

import java.util.Set;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import static be.cytomine.common.repository.http.ReviewedAnnotationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface ReviewedAnnotationHttpContract {
    String ROOT_PATH = "/reviewed-annotations";

    @PutExchange("/terms/{reviewedAnnotationTermsId}")
    Set<Long> replaceAllTermIds(@PathVariable long reviewedAnnotationTermsId, @RequestParam long userId,
                                @RequestBody Set<Long> newLinks);


}
