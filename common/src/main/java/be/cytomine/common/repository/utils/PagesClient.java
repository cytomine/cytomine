package be.cytomine.common.repository.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PagesClient {

    public <T> Set<T> callAllPages(Function<Pageable, Page<T>> caller) {
        Pageable page = null;
        Page<T> currentPage = caller.apply(page);
        Set<T> result = new HashSet<>(currentPage.getContent());

        while (currentPage.hasNext()) {
            currentPage = caller.apply(currentPage.nextPageable());
            result.addAll(currentPage.getContent());
        }
        return result;

    }

}
