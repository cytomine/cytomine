package be.cytomine.utils;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

public class PageUtils {

    public static <T> Page<T> buildPageFromPageResults(List<T> data, Long max, Long offset, Long total) {
        return new PageImpl<>(
            data,
            new OffsetBasedPageRequest(offset, (max == 0 ? Integer.MAX_VALUE : max.intValue()), Sort.unsorted()),
            total
        );
    }
}
