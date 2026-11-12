package be.cytomine.controller.repository;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class PageableHttpServiceArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(
        Object argument,
        MethodParameter parameter,
        HttpRequestValues.Builder requestValues
    ) {
        if (!(argument instanceof Pageable pageable)) {
            return false;
        }

        requestValues.addRequestParameter(
            "page",
            String.valueOf(pageable.isPaged() ? pageable.getPageNumber() : 0)
        );
        requestValues.addRequestParameter(
            "size",
            String.valueOf(pageable.isPaged() ? pageable.getPageSize() : Integer.MAX_VALUE)
        );

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order ->
                requestValues.addRequestParameter(
                    "sort",
                    order.getProperty() + "," + order.getDirection().name().toLowerCase()
                )
            );
        }
        return true;
    }
}