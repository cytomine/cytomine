package be.cytomine.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.dto.meilisearch.MeiliSearchFacetsResponse;
import be.cytomine.dto.meilisearch.MeiliSearchImageResponse;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.MeiliSearchService;


@RestController
@RequestMapping("/api/meilisearch")
@RequiredArgsConstructor
public class MeiliSearchController {

    private static final int ACCESSIBLE_STORAGES_PAGE_SIZE = 1000;

    private final CurrentUserService currentUserService;
    private final MeiliSearchService meiliSearchService;
    private final StorageHttpContract storageHttpContract;

    @GetMapping("/search")
    public ResponseEntity<List<MeiliSearchImageResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(name = "filters", required = false) List<String> filters,
            @RequestParam(name = "filter", required = false) List<String> filter,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {

        List<String> allFilters = Stream.of(filters, filter)
            .filter(list -> list != null && !list.isEmpty())
            .flatMap(List::stream)
            .toList();

        List<MeiliSearchImageResponse>
            results = meiliSearchService.search(query, allFilters, limit, offset);
        return ResponseEntity.ok(results);

    }

    @GetMapping("/image/{imageid}")
    public ResponseEntity<MeiliSearchImageResponse> getDocument(
            @PathVariable String imageid) {

        MeiliSearchImageResponse document = meiliSearchService.getImage(imageid);
        if (document != null) {
            return ResponseEntity.ok(document);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/facets")
    public MeiliSearchFacetsResponse getFacets(
        @RequestParam Optional<String> project
    ) {

        if (project.isPresent() && !project.orElse("").isBlank()) {
            return meiliSearchService.getFacetDistribution(project);
        }

        long userId = currentUserService.getCurrentUser().id();
        return meiliSearchService.getFacetDistribution(Optional.empty(), accessibleStorageIds(userId));

    }

    private List<Long> accessibleStorageIds(long userId) {
        List<Long> storageIds = new ArrayList<>();
        int pageNumber = 0;
        Page<StorageResponse> page;
        do {
            page = storageHttpContract.getAll(userId, PageRequest.of(pageNumber++, ACCESSIBLE_STORAGES_PAGE_SIZE));
            page.getContent().stream().map(StorageResponse::id).forEach(storageIds::add);
        } while (page.hasNext());
        return storageIds;
    }
}
