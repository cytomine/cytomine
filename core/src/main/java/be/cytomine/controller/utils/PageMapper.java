package be.cytomine.controller.utils;

import java.util.List;

import be.cytomine.common.repository.model.command.HttpCommandResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <T> CollectionResponse<T> toCollectionResponse(Page<T> page) {
        return new CollectionResponse<>(page.getContent(),
            page.getPageable().isPaged() ? page.getPageable().getOffset() : 0L, page.getSize(), page.getTotalElements(),
            page.getTotalPages());
    }

    public <T> CollectionResponse<T> toCollectionResponse(List<T> list) {
        return new CollectionResponse<>(list, 0L, list.size(), list.size(), 1);
    }

    public <T> HttpCommandResponse<T> toCommandResponse(String message, T data) {
        return new HttpCommandResponse<>(message, null, false, data, -1);
    }
}
