package be.cytomine.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;

@Service
@RequiredArgsConstructor
public class AccessibleStorageService {

    private static final int PAGE_SIZE = 1000;

    private final StorageHttpContract storageHttpContract;

    public List<Long> ids(long userId) {
        List<Long> storageIds = new ArrayList<>();
        int pageNumber = 0;
        Page<StorageResponse> page;
        do {
            page = storageHttpContract.getAll(userId, PageRequest.of(pageNumber++, PAGE_SIZE));
            page.getContent().stream().map(StorageResponse::id).forEach(storageIds::add);
        } while (page.hasNext());
        return storageIds;
    }
}