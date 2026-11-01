package be.cytomine.controller.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <T> CollectionResponse<T> toCollectionResponse(Page<T> nullablePage) {
        return Optional.ofNullable(nullablePage).map(
            page -> new CollectionResponse<>(Optional.ofNullable(page.getContent()).orElse(List.of()),
                page.getPageable().isPaged() ? page.getPageable().getOffset() : 0L, page.getSize(),
                page.getTotalElements(), page.getTotalPages())
        ).orElse(new CollectionResponse<>(List.of(), 0, 0, 0, 1));
    }

    public <T, F> FilteredCollectionResponse<T, F> toFilteredCollectionResponse(Page<T> nullablePage, F filters) {
        return Optional.ofNullable(nullablePage).map(
            page -> new FilteredCollectionResponse<>(Optional.ofNullable(page.getContent()).orElse(List.of()),
                page.getPageable().isPaged() ? page.getPageable().getOffset() : 0L, page.getSize(),
                page.getTotalElements(), page.getTotalPages(), filters)
        ).orElse(new FilteredCollectionResponse<>(List.of(), 0, 0, 0, 1, filters));
    }
}
