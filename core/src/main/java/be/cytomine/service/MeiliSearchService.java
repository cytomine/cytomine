package be.cytomine.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.meilisearch.sdk.model.Searchable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.dto.meilisearch.MeiliSearchFacetsResponse;
import be.cytomine.dto.meilisearch.MeiliSearchImageResponse;
import be.cytomine.dto.meilisearch.SearchWindow;
import be.cytomine.exceptions.SearchException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeiliSearchService {

    private static final int SEARCH_PAGE_SIZE = 1000;
    private static final String[] ABSTRACT_IMAGE_ID_ATTRIBUTE = {"image.abstract_image_id"};

    @Value("${meilisearch.index_id}")
    private String indexId;

    private final Client meiliSearchClient;
    private final ObjectMapper objectMapper;
    private final StorageHttpContract storageHttpContract;

    @PostConstruct
    public void createIndexIfNotExists() {
        try {
            for (Index index : meiliSearchClient.getIndexes().getResults()) {
                if (indexId.equals(index.getUid())) {
                    return;
                }
            }
            meiliSearchClient.createIndex(indexId);
            log.info("Created MeiliSearch index '{}'", indexId);
        } catch (Exception e) {
            log.warn("Could not create MeiliSearch index '{}' at startup: {}", indexId, e.getMessage());
        }
    }

    private List<Long> accessibleStorageIds(long userId) {
        Page<StorageResponse> page = storageHttpContract.getAll(userId, PageRequest.of(0, Integer.MAX_VALUE));
        return page.getContent().stream().map(StorageResponse::id).toList();
    }

    public List<MeiliSearchImageResponse> search(
        String query,
        List<String> filters,
        int limit,
        int offset
    ) {
        Index index = getIndexOrThrow(indexId);

        try {
            SearchRequest searchRequest = buildSearchRequest(query, filters)
                .setLimit(limit)
                .setOffset(offset);

            Searchable result = index.search(searchRequest);
            return result.getHits().stream()
                .map(hit -> objectMapper.convertValue(hit, MeiliSearchImageResponse.class))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Could not search for '{}'", query, e);
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    public Set<Long> searchImageIds(String query, List<String> filters) {
        return searchImageIds(query, filters, null);
    }

    public Set<Long> searchImageIds(String query, List<String> filters, List<Long> storageIds) {
        Index index = getIndexOrThrow(indexId);

        try {
            SearchResultPaginated firstPage = searchPage(index, query, filters, 1, storageIds);

            Set<Long> abstractImageIds = collectAbstractImageIds(firstPage);
            for (int page = 2; page <= firstPage.getTotalPages(); page++) {
                abstractImageIds.addAll(collectAbstractImageIds(searchPage(index, query, filters, page, storageIds)));
            }

            return abstractImageIds;
        } catch (Exception e) {
            log.error("Could not search for '{}'", query, e);
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    public SearchWindow searchWindow(String query, List<String> filters, String datasetAlias, int page, int size) {
        return searchWindow(query, filters, datasetAlias, null, page, size);
    }

    public SearchWindow searchWindow(
        long userId,
        String query,
        List<String> filters,
        int page,
        int size
    ) {
        return searchWindow(query, filters, null, accessibleStorageIds(userId), page, size);
    }

    public SearchWindow searchWindow(
        String query,
        List<String> filters,
        String datasetAlias,
        List<Long> storageIds,
        int page,
        int size
    ) {
        Index index = getIndexOrThrow(indexId);
        try {
            SearchRequest searchRequest = buildSearchRequest(query, filters, datasetAlias, storageIds)
                .setPage(page)
                .setHitsPerPage(size)
                .setAttributesToRetrieve(ABSTRACT_IMAGE_ID_ATTRIBUTE);
            SearchResultPaginated result = (SearchResultPaginated) index.search(searchRequest);
            List<Long> ids = decodeAbstractImageIdsInOrder(result);
            long totalHits = result.getTotalHits();
            return new SearchWindow(ids, totalHits);
        } catch (Exception e) {
            log.error("Could not search for '{}'", query, e);
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    private List<Long> decodeAbstractImageIdsInOrder(Searchable result) {
        List<Long> ids = new ArrayList<>();
        for (Map<String, Object> hit : result.getHits()) {
            Object image = hit.get("image");
            if (image instanceof Map<?, ?> img) {
                Object idObj = img.get("abstract_image_id");
                if (idObj instanceof Number num) {
                    long id = num.longValue();
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
            }
        }
        return ids;
    }

    private SearchResultPaginated searchPage(
        Index index,
        String query,
        List<String> filters,
        int page,
        List<Long> storageIds
    ) {
        SearchRequest searchRequest = buildSearchRequest(query, filters, null, storageIds)
            .setPage(page)
            .setHitsPerPage(SEARCH_PAGE_SIZE)
            .setAttributesToRetrieve(ABSTRACT_IMAGE_ID_ATTRIBUTE);
        return (SearchResultPaginated) index.search(searchRequest);
    }

    private Set<Long> collectAbstractImageIds(Searchable result) {
        return result.getHits().stream()
            .map(hit -> hit.get("image"))
            .filter(image -> image instanceof Map<?, ?>)
            .map(image -> ((Map<?, ?>) image).get("abstract_image_id"))
            .filter(Objects::nonNull)
            .map(id -> ((Number) id).longValue())
            .collect(Collectors.toCollection(HashSet::new));
    }

    private SearchRequest buildSearchRequest(String query, List<String> filters) {
        return buildSearchRequest(query, filters, null, null);
    }

    private SearchRequest buildSearchRequest(
        String query,
        List<String> filters,
        String datasetAlias,
        List<Long> storageIds
    ) {
        SearchRequest searchRequest = new SearchRequest(query != null ? query : "");

        List<String> allFilters = new ArrayList<>(filters);
        if (datasetAlias != null && !datasetAlias.isBlank()) {
            allFilters.add("dataset.alias:" + datasetAlias);
        }
        if (storageIds != null) {
            allFilters.add(imageStorageIdsFilter(storageIds));
        }

        if (!allFilters.isEmpty()) {
            String meiliFilter = allFilters.stream()
                .map(this::normalizeFilter)
                .filter(f -> f != null && !f.trim().isEmpty())
                .collect(Collectors.joining(" AND "));
            if (!meiliFilter.isEmpty()) {
                searchRequest.setFilter(new String[]{meiliFilter});
            }
        }

        return searchRequest;
    }

    public MeiliSearchImageResponse getImage(String imageId) {
        Index index = getIndexOrThrow(indexId);
        try {
            Object document = index.getDocument(imageId, Object.class);
            return objectMapper.convertValue(document, MeiliSearchImageResponse.class);
        } catch (Exception e) {
            throw new SearchException("MeiliSearch getDocument failed", 500, e.getMessage());
        }
    }

    public MeiliSearchFacetsResponse getFacetDistribution(Optional<String> projectDatasetAlias) {
        return getFacetDistribution(projectDatasetAlias, null);
    }

    public MeiliSearchFacetsResponse getFacetDistribution(long userId) {
        return getFacetDistribution(Optional.empty(), accessibleStorageIds(userId));
    }

    public MeiliSearchFacetsResponse getFacetDistribution(Optional<String> projectDatasetAlias, List<Long> storageIds) {

        Index index = getIndexOrThrow(indexId);
        try {
            String[] attributes = index.getFilterableAttributesSettings();

            List<String> filters = new ArrayList<>();
            projectDatasetAlias.filter(p -> !p.isBlank()).map(p -> "dataset.alias:" + p).ifPresent(filters::add);
            if (storageIds != null) {
                filters.add(imageStorageIdsFilter(storageIds));
            }
            SearchRequest searchRequest = buildSearchRequest(null, filters)
                .setFacets(attributes)
                .setLimit(0);

            SearchResult result = (SearchResult) index.search(searchRequest);
            return objectMapper.convertValue(result.getFacetDistribution(), MeiliSearchFacetsResponse.class);
        } catch (Exception e) {
            throw new SearchException("MeiliSearch getFacetDistribution failed", 500, e.getMessage());
        }
    }

    public Index getIndexOrThrow(String indexUid) {
        Index[] indexes = meiliSearchClient.getIndexes().getResults();
        for (Index index : indexes) {
            if (indexUid.equals(index.getUid())) {
                return index;
            }
        }
        throw new SearchException("MeiliSearch index not found", 404, "index not found");
    }

    private String imageStorageIdsFilter(List<Long> storageIds) {
        List<Long> effectiveStorageIds = storageIds.isEmpty() ? List.of(-1L) : storageIds;
        String storageValues = effectiveStorageIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        return "image.storage_id IN [" + storageValues + "]";
    }

    private String normalizeFilter(String filter) {
        if (filter == null) {
            return null;
        }

        String trimmed = filter.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String lowered = trimmed.toLowerCase(Locale.ROOT);
        if (trimmed.contains("=") || lowered.contains(" != ") || lowered.contains(" > ")
            || lowered.contains(" < ") || lowered.contains(" >= ") || lowered.contains(" <= ")) {
            return trimmed;
        }

        int separatorIndex = trimmed.indexOf(':');
        if (separatorIndex > 0 && separatorIndex < trimmed.length() - 1) {
            String field = trimmed.substring(0, separatorIndex).trim();
            String value = trimmed.substring(separatorIndex + 1).trim();
            if (!value.startsWith("\"") && !value.endsWith("\"")) {
                value = "\"" + value.replace("\"", "\\\"") + "\"";
            }
            return field + " = " + value;
        }

        return trimmed;
    }
}
