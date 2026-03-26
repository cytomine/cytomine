package be.cytomine.controller.utils;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.HttpCommandResponse;

@Component
public class PageMapper {

    public <T> CollectionResponse<T> toCollectionResponse(Page<T> page) {
        return new CollectionResponse<>(page.getContent(),
            page.getPageable().isPaged() ? page.getPageable().getOffset() : 0L, page.getSize(), page.getTotalElements(),
            page.getTotalPages());
    }
}
