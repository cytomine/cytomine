package be.cytomine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.TaskInfo;
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
    private static final int DOCUMENT_WRITE_BATCH_SIZE = 200;
    private static final int FILTER_CHUNK_SIZE = 1000;
    private static final String[] ABSTRACT_IMAGE_ID_ATTRIBUTE = {"image.abstract_image_id"};
    private static final String PROJECTS_ATTRIBUTE = "image.projects";
    private static final String[] ALL_ATTRIBUTES = {"*"};

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

    public Set<Long> searchImageIds(long userId, String query, List<String> filters) {
        return searchImageIds(query, filters, accessibleStorageIds(userId));
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

    public SearchWindow searchWindow(String query, List<String> filters, String projectName, int page, int size) {
        return searchWindow(query, filters, projectName, null, page, size);
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
        String projectName,
        List<Long> storageIds,
        int page,
        int size
    ) {
        Index index = getIndexOrThrow(indexId);
        try {
            SearchRequest searchRequest = buildSearchRequest(query, filters, projectName, storageIds)
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
        String projectName,
        List<Long> storageIds
    ) {
        SearchRequest searchRequest = new SearchRequest(query != null ? query : "");

        List<String> allFilters = new ArrayList<>(filters);
        if (projectName != null && !projectName.isBlank()) {
            allFilters.add(PROJECTS_ATTRIBUTE + ":" + projectName);
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

    /**
     * Append a project name to the {@code image.projects} list of every document whose abstract image is
     * part of the given ids. The list is a live membership: each project name appears at most once.
     *
     * @return The number of documents actually updated
     */
    public int addProjectToImages(Collection<Long> abstractImageIds, String projectName) {
        Index index = getIndexOrThrow(indexId);
        try {
            List<Map<String, Object>> documents = fetchDocumentsByAbstractImageIds(index, abstractImageIds);
            int updated = 0;
            for (Map<String, Object> document : documents) {
                if (appendProjectName(document, projectName)) {
                    updated++;
                }
            }
            if (updated > 0) {
                writeDocuments(index, documents);
            }
            return updated;
        } catch (MeilisearchException | JsonProcessingException e) {
            log.error("Could not add project '{}' to images", projectName, e);
            throw new SearchException("MeiliSearch write failed", 500, e.getMessage());
        }
    }

    /**
     * Remove a project name from the {@code image.projects} list of every document whose abstract image is
     * part of the given ids (image removed from a project, or project deletion).
     *
     * @return The number of documents actually updated
     */
    public int removeProjectFromImages(Collection<Long> abstractImageIds, String projectName) {
        Index index = getIndexOrThrow(indexId);
        try {
            List<Map<String, Object>> documents = fetchDocumentsByAbstractImageIds(index, abstractImageIds);
            int updated = 0;
            for (Map<String, Object> document : documents) {
                if (removeProjectName(document, projectName)) {
                    updated++;
                }
            }
            if (updated > 0) {
                writeDocuments(index, documents);
            }
            return updated;
        } catch (MeilisearchException | JsonProcessingException e) {
            log.error("Could not remove project '{}' from images", projectName, e);
            throw new SearchException("MeiliSearch write failed", 500, e.getMessage());
        }
    }

    /**
     * Replace an old project name with a new one in the {@code image.projects} list of every document that
     * still contains the old name (project renamed).
     *
     * @return The number of documents actually updated
     */
    public int renameProjectInImages(String oldProjectName, String newProjectName) {
        Index index = getIndexOrThrow(indexId);
        try {
            String filter = normalizeFilter(PROJECTS_ATTRIBUTE + ":" + oldProjectName);
            List<Map<String, Object>> documents = searchFullDocuments(index, filter);
            int updated = 0;
            for (Map<String, Object> document : documents) {
                if (replaceProjectName(document, oldProjectName, newProjectName)) {
                    updated++;
                }
            }
            if (updated > 0) {
                writeDocuments(index, documents);
            }
            return updated;
        } catch (MeilisearchException | JsonProcessingException e) {
            log.error("Could not rename project '{}' to '{}' in images", oldProjectName, newProjectName, e);
            throw new SearchException("MeiliSearch write failed", 500, e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchDocumentsByAbstractImageIds(Index index, Collection<Long> abstractImageIds)
        throws MeilisearchException {
        List<Long> ids = new ArrayList<>(abstractImageIds);
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int from = 0; from < ids.size(); from += FILTER_CHUNK_SIZE) {
            List<Long> chunk = ids.subList(from, Math.min(from + FILTER_CHUNK_SIZE, ids.size()));
            String values = chunk.stream().map(String::valueOf).collect(Collectors.joining(", "));
            documents.addAll(searchFullDocuments(index, "image.abstract_image_id IN [" + values + "]"));
        }
        return documents;
    }

    /**
     * Search all full documents matching a filter, walking every page.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchFullDocuments(Index index, String filter) throws MeilisearchException {
        List<Map<String, Object>> documents = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest("")
            .setFilter(new String[]{filter})
            .setAttributesToRetrieve(ALL_ATTRIBUTES);
        int page = 1;
        SearchResultPaginated result;
        do {
            result = (SearchResultPaginated) index.search(
                searchRequest.setPage(page).setHitsPerPage(SEARCH_PAGE_SIZE)
            );
            documents.addAll(result.getHits());
            page++;
        } while (page <= result.getTotalPages());
        return documents;
    }

    private Map<String, Object> imageMap(Map<String, Object> document) {
        Object image = document.get("image");
        return image instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @SuppressWarnings("unchecked")
    private List<String> projectNames(Map<String, Object> document) {
        Map<String, Object> image = imageMap(document);
        if (image == null) {
            return new ArrayList<>();
        }
        Object projects = image.get("projects");
        if (projects instanceof List<?> list) {
            return new ArrayList<>((List<String>) list);
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private boolean writeProjectNames(Map<String, Object> document, List<String> projectNames) {
        Map<String, Object> image = imageMap(document);
        if (image == null) {
            return false;
        }
        Object previous = image.get("projects");
        image.put("projects", projectNames);
        return !Objects.equals(previous, projectNames);
    }

    private boolean appendProjectName(Map<String, Object> document, String projectName) {
        List<String> projectNames = projectNames(document);
        if (projectNames.contains(projectName)) {
            return false;
        }
        projectNames.add(projectName);
        return writeProjectNames(document, projectNames);
    }

    private boolean removeProjectName(Map<String, Object> document, String projectName) {
        List<String> projectNames = projectNames(document);
        if (!projectNames.contains(projectName)) {
            return false;
        }
        projectNames.remove(projectName);
        return writeProjectNames(document, projectNames);
    }

    private boolean replaceProjectName(Map<String, Object> document, String oldProjectName, String newProjectName) {
        List<String> projectNames = projectNames(document);
        if (!projectNames.contains(oldProjectName)) {
            return false;
        }
        int firstIndex = projectNames.indexOf(oldProjectName);
        while (projectNames.remove(oldProjectName)) {
            // remove every occurrence
        }
        if (!projectNames.contains(newProjectName)) {
            projectNames.add(Math.min(firstIndex, projectNames.size()), newProjectName);
        }
        return writeProjectNames(document, projectNames);
    }

    private void writeDocuments(Index index, List<Map<String, Object>> documents)
        throws MeilisearchException, JsonProcessingException {
        for (int from = 0; from < documents.size(); from += DOCUMENT_WRITE_BATCH_SIZE) {
            List<Map<String, Object>> batch = documents.subList(
                from, Math.min(from + DOCUMENT_WRITE_BATCH_SIZE, documents.size())
            );
            TaskInfo taskInfo = index.addDocuments(objectMapper.writeValueAsString(batch));
            index.waitForTask(taskInfo.getTaskUid());
        }
    }

    public MeiliSearchFacetsResponse getFacetDistribution(Optional<String> projectName) {
        return getFacetDistribution(projectName, null);
    }

    public MeiliSearchFacetsResponse getFacetDistribution(long userId) {
        return getFacetDistribution(Optional.empty(), accessibleStorageIds(userId));
    }

    public MeiliSearchFacetsResponse getFacetDistribution(Optional<String> projectName, List<Long> storageIds) {

        Index index = getIndexOrThrow(indexId);
        try {
            String[] attributes = index.getFilterableAttributesSettings();

            List<String> filters = new ArrayList<>();
            projectName.filter(p -> !p.isBlank()).map(p -> PROJECTS_ATTRIBUTE + ":" + p).ifPresent(filters::add);
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
