package be.cytomine.controller.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <T> CollectionResponse<T> toCollectionResponse(Page<T> page) {
        return new CollectionResponse<>(Optional.ofNullable(page).map(Slice::getContent).orElse(List.of()),
            page.getPageable().isPaged() ? page.getPageable().getOffset() : 0L, page.getSize(), page.getTotalElements(),
            page.getTotalPages());
    }
}
