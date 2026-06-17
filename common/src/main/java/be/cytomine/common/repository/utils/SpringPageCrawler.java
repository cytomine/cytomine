package be.cytomine.common.repository.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class SpringPageCrawler {

    public <T> Set<T> getAllPages(Function<Pageable, Page<T>> caller) {
        Pageable next = PageRequest.of(0, 50);
        boolean lastCallHasResult = true;
        HashSet<T> allResults = new HashSet<>();
        while (lastCallHasResult) {
            Set<T> currentResult = caller.apply(next).toSet();
            lastCallHasResult = !currentResult.isEmpty();
            allResults.addAll(currentResult);
            next = next.next();
        }
        return allResults;
    }

}
