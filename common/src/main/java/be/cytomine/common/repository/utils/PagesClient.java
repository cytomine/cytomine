package be.cytomine.common.repository.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PagesClient {


    public <T> Set<T> callAllPages(BiFunction<Integer, Integer, Page<T>> caller) {

        Page<T> currentPage = caller.apply(0, 20);
        Set<T> result = new HashSet<>(currentPage.getContent());

        while (currentPage.hasNext()) {
            currentPage = caller.apply(currentPage.nextPageable().getPageNumber(), currentPage.getSize());
            result.addAll(currentPage.getContent());
        }
        return result;

    }

}
