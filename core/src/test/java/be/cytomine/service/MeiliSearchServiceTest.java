package be.cytomine.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.Results;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.SearchResultPaginated;
import com.meilisearch.sdk.model.TaskInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import be.cytomine.common.repository.http.StorageHttpContract;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.dto.meilisearch.SearchWindow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeiliSearchServiceTest {

    private static final String INDEX_ID = "test_index";

    @Mock
    private Client meiliSearchClient;

    @Mock
    private StorageHttpContract storageHttpContract;

    @InjectMocks
    private MeiliSearchService meiliSearchService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(meiliSearchService, "indexId", INDEX_ID);
        ReflectionTestUtils.setField(meiliSearchService, "objectMapper", new ObjectMapper());
    }

    private void mockExistingIndexes(Index... indexes) {
        Results<Index> results = mock();
        when(results.getResults()).thenReturn(indexes);
        when(meiliSearchClient.getIndexes()).thenReturn(results);
    }

    @Test
    public void createIndexIfNotExistsShouldCreateAndConfigureIndexWhenMissing() {
        mockExistingIndexes();
        Index created = mockConfiguredFilterableIndex();
        mockUpdateFilterableAttributes(created);
        when(meiliSearchClient.getIndex(INDEX_ID)).thenReturn(created);

        meiliSearchService.createIndexIfNotExists();

        verify(meiliSearchClient, times(1)).createIndex(INDEX_ID);
        verify(created).updateFilterableAttributesSettings(argThat(attrs -> attrs.length == 3
            && Arrays.asList(attrs).containsAll(List.of(
                "image.abstract_image_id", "image.storage_id", "image.projects"))));
    }

    @Test
    public void createIndexIfNotExistsShouldNotCreateIndexWhenAlreadyPresent() {
        Index existing = mockConfiguredFilterableIndex(new String[]{
            "image.abstract_image_id", "image.storage_id", "image.projects"});
        when(existing.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(existing);

        meiliSearchService.createIndexIfNotExists();

        verify(meiliSearchClient, never()).createIndex(INDEX_ID);
        verify(existing, never()).updateFilterableAttributesSettings(any());
    }

    @Test
    public void createIndexIfNotExistsShouldMergeExistingFilterableAttributes() {
        Index existing = mockConfiguredFilterableIndex(new String[]{
            "image.abstract_image_id", "image.storage_id"});
        mockUpdateFilterableAttributes(existing);
        when(existing.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(existing);

        meiliSearchService.createIndexIfNotExists();

        verify(existing).updateFilterableAttributesSettings(argThat(attrs -> attrs.length == 3
            && Arrays.asList(attrs).containsAll(List.of(
                "image.abstract_image_id", "image.storage_id", "image.projects"))));
    }

    @Test
    public void createIndexIfNotExistsShouldSwallowFailures() {
        when(meiliSearchClient.getIndexes()).thenThrow(new RuntimeException("MeiliSearch unreachable"));

        assertDoesNotThrow(() -> meiliSearchService.createIndexIfNotExists());

        verify(meiliSearchClient, never()).createIndex(INDEX_ID);
    }

    private Index mockConfiguredFilterableIndex(String... existingFilterableAttributes) {
        Index index = mock(Index.class);
        when(index.getFilterableAttributesSettings()).thenReturn(existingFilterableAttributes);
        return index;
    }

    private void mockUpdateFilterableAttributes(Index index) {
        TaskInfo taskInfo = mock(TaskInfo.class);
        when(taskInfo.getTaskUid()).thenReturn(1);
        when(index.updateFilterableAttributesSettings(any())).thenReturn(taskInfo);
    }

    private Index mockSearchableIndex(List<HashMap<String, Object>> hits) {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);

        SearchResultPaginated searchable = mock(SearchResultPaginated.class);
        when(searchable.getHits()).thenReturn(new ArrayList<>(hits));
        when(searchable.getTotalPages()).thenReturn(1);
        when(index.search(any(SearchRequest.class))).thenReturn(searchable);
        return index;
    }

    private HashMap<String, Object> hitWithAbstractImageId(Object abstractImageId) {
        HashMap<String, Object> image = new HashMap<>();
        image.put("abstract_image_id", abstractImageId);
        HashMap<String, Object> hit = new HashMap<>();
        hit.put("image", image);
        return hit;
    }

    @Test
    public void searchImageIdsShouldCollectAbstractImageIds() {
        mockSearchableIndex(List.of(hitWithAbstractImageId(11), hitWithAbstractImageId(22)));

        Set<Long> ids = meiliSearchService.searchImageIds("query", List.of());

        assertEquals(Set.of(11L, 22L), ids);
    }

    @Test
    public void searchImageIdsShouldDeduplicateAbstractImageIds() {
        mockSearchableIndex(List.of(hitWithAbstractImageId(11), hitWithAbstractImageId(11)));

        Set<Long> ids = meiliSearchService.searchImageIds("", List.of());

        assertEquals(Set.of(11L), ids);
    }

    @Test
    public void searchImageIdsShouldIgnoreHitsWithoutAbstractImageId() {
        HashMap<String, Object> withoutImage = new HashMap<>();
        withoutImage.put("id", "some-id");

        mockSearchableIndex(List.of(
            hitWithAbstractImageId(11),
            hitWithAbstractImageId(null),
            withoutImage
        ));

        Set<Long> ids = meiliSearchService.searchImageIds("", List.of());

        assertEquals(Set.of(11L), ids);
    }

    @Test
    public void searchImageIdsShouldReturnEmptyWhenNoHits() {
        mockSearchableIndex(List.of());

        Set<Long> ids = meiliSearchService.searchImageIds("query", List.of());

        assertTrue(ids.isEmpty());
    }

    @Test
    public void searchImageIdsShouldCollectAcrossPages() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);

        ArrayList<HashMap<String, Object>> page1Hits = new ArrayList<>();
        page1Hits.add(hitWithAbstractImageId(11));
        SearchResultPaginated page1 = mock(SearchResultPaginated.class);
        when(page1.getTotalPages()).thenReturn(2);
        when(page1.getHits()).thenReturn(page1Hits);

        ArrayList<HashMap<String, Object>> page2Hits = new ArrayList<>();
        page2Hits.add(hitWithAbstractImageId(22));
        SearchResultPaginated page2 = mock(SearchResultPaginated.class);
        when(page2.getHits()).thenReturn(page2Hits);

        when(index.search(any(SearchRequest.class))).thenReturn(page1, page2);

        Set<Long> ids = meiliSearchService.searchImageIds("", List.of());

        assertEquals(Set.of(11L, 22L), ids);
        verify(index, times(2)).search(any(SearchRequest.class));
    }

    private String captureSearchFilter(Index index) {
        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(index).search(captor.capture());
        String[] filters = captor.getValue().getFilter();
        return filters == null ? "" : String.join(" AND ", filters);
    }

    @Test
    public void searchImageIdsShouldScopeByStorageIds() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);
        when(index.search(any(SearchRequest.class))).thenReturn(mock(SearchResultPaginated.class));

        meiliSearchService.searchImageIds("query", List.of(), List.of(7L, 8L));

        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [7, 8]"));
    }

    @Test
    public void searchWindowShouldScopeByStorageIds() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);

        SearchResultPaginated searchable = mock(SearchResultPaginated.class);
        when(searchable.getHits()).thenReturn(new ArrayList<>(List.of(
            hitWithAbstractImageId(11), hitWithAbstractImageId(22)
        )));
        when(searchable.getTotalHits()).thenReturn(42);
        when(index.search(any(SearchRequest.class))).thenReturn(searchable);

        SearchWindow window =
            meiliSearchService.searchWindow("query", List.of(), null, List.of(7L, 8L), 2, 20);

        assertEquals(List.of(11L, 22L), window.abstractImageIds());
        assertEquals(42L, window.totalHits());
        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [7, 8]"));
    }

    @Test
    public void searchWindowShouldScopeToImpossibleStorageWhenScopeEmpty() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);

        SearchResultPaginated searchable = mock(SearchResultPaginated.class);
        when(searchable.getHits()).thenReturn(new ArrayList<>());
        when(searchable.getTotalHits()).thenReturn(0);
        when(index.search(any(SearchRequest.class))).thenReturn(searchable);

        meiliSearchService.searchWindow("query", List.of(), null, List.of(), 1, 20);

        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [-1]"));
    }

    private Page<StorageResponse> pageOfStorages(Long... ids) {
        List<StorageResponse> storages = Arrays.stream(ids)
            .map(id -> new StorageResponse(id, 1L, "storage_" + id, LocalDateTime.of(2024, 1, 1, 0, 0),
                Optional.empty(), Optional.empty()))
            .toList();
        return new PageImpl<>(storages);
    }

    private Index mockSearchableIndexWithEmptyResults() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);

        SearchResultPaginated searchable = mock(SearchResultPaginated.class);
        when(searchable.getHits()).thenReturn(new ArrayList<>());
        when(searchable.getTotalHits()).thenReturn(0);
        when(index.search(any(SearchRequest.class))).thenReturn(searchable);
        return index;
    }

    @Test
    public void searchWindowShouldResolveUserStoragesInOneCall() {
        when(storageHttpContract.getAll(any())).thenReturn(pageOfStorages(7L, 8L));

        Index index = mockSearchableIndexWithEmptyResults();

        meiliSearchService.searchWindow("query", List.of(), 1, 20);

        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [7, 8]"));
        verify(storageHttpContract, times(1)).getAll(any());
    }

    @Test
    public void searchWindowShouldScopeToImpossibleStorageWhenUserHasNoStorage() {
        when(storageHttpContract.getAll(any())).thenReturn(pageOfStorages());

        Index index = mockSearchableIndexWithEmptyResults();

        meiliSearchService.searchWindow("query", List.of(), 1, 20);

        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [-1]"));
        verify(storageHttpContract, times(1)).getAll(any());
    }

    @Test
    public void getFacetDistributionShouldResolveUserStorages() {
        when(storageHttpContract.getAll(any())).thenReturn(pageOfStorages(7L, 8L));

        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);
        when(index.getFilterableAttributesSettings()).thenReturn(new String[]{"image.storage_id"});
        SearchResult result = mock(SearchResult.class);
        when(result.getFacetDistribution()).thenReturn(Map.of());
        when(index.search(any(SearchRequest.class))).thenReturn(result);

        meiliSearchService.getFacetDistribution();

        assertTrue(captureSearchFilter(index).contains("image.storage_id IN [7, 8]"));
        verify(storageHttpContract, times(1)).getAll(any());
    }

    @Test
    public void getFacetDistributionShouldScopeByProjectName() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);
        when(index.getFilterableAttributesSettings()).thenReturn(new String[]{"image.projects"});
        SearchResult result = mock(SearchResult.class);
        when(result.getFacetDistribution()).thenReturn(Map.of());
        when(index.search(any(SearchRequest.class))).thenReturn(result);

        meiliSearchService.getFacetDistribution(Optional.of("my project"));

        assertTrue(captureSearchFilter(index).contains("image.projects = \"my project\""));
    }

    @Test
    public void searchWindowShouldScopeByProjectName() {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);
        when(index.search(any(SearchRequest.class))).thenReturn(mock(SearchResultPaginated.class));

        meiliSearchService.searchWindow("query", List.of(), "my project", null, 1, 20);

        assertTrue(captureSearchFilter(index).contains("image.projects = \"my project\""));
    }

    private HashMap<String, Object> hitWithProjects(Long abstractImageId, String... projects) {
        HashMap<String, Object> image = new HashMap<>();
        image.put("abstract_image_id", abstractImageId);
        image.put("projects", Arrays.asList(projects));
        HashMap<String, Object> hit = new HashMap<>();
        hit.put("id", "id-" + abstractImageId);
        hit.put("image", image);
        return hit;
    }

    private Index mockWriteIndex(HashMap<String, Object>... hits) {
        Index index = mock(Index.class);
        when(index.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(index);
        SearchResultPaginated searchable = mock(SearchResultPaginated.class);
        when(searchable.getHits()).thenReturn(new ArrayList<>(Arrays.asList(hits)));
        when(searchable.getTotalPages()).thenReturn(hits.length == 0 ? 0 : 1);
        when(index.search(any(SearchRequest.class))).thenReturn(searchable);
        TaskInfo taskInfo = mock(TaskInfo.class);
        lenient().when(taskInfo.getTaskUid()).thenReturn(42);
        lenient().when(index.addDocuments(anyString())).thenReturn(taskInfo);
        return index;
    }

    private List<Object> documentProjects(Index index) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(index).addDocuments(captor.capture());
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> docs = mapper.readValue(
                captor.getValue(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {}
            );
            @SuppressWarnings("unchecked")
            Object image = docs.get(0).get("image");
            @SuppressWarnings("unchecked")
            List<Object> projects = (List<Object>) ((Map<String, Object>) image).get("projects");
            return projects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addProjectToImagesShouldMergeProjectName() {
        Index index = mockWriteIndex(hitWithProjects(11L, "Existing"));

        int updated = meiliSearchService.addProjectToImages(List.of(11L), "NewProject");

        assertEquals(1, updated);
        assertEquals(List.of("Existing", "NewProject"), documentProjects(index));
        verify(index).waitForTask(42);
    }

    @Test
    public void addProjectToImagesShouldNotRewriteWhenAlreadyPresent() {
        Index index = mockWriteIndex(hitWithProjects(11L, "NewProject"));

        int updated = meiliSearchService.addProjectToImages(List.of(11L), "NewProject");

        assertEquals(0, updated);
        verify(index, never()).addDocuments(anyString());
    }

    @Test
    public void addProjectToImagesShouldHandleMissingProjectsList() {
        Index index = mockWriteIndex(hitWithAbstractImageId(11L));

        int updated = meiliSearchService.addProjectToImages(List.of(11L), "NewProject");

        assertEquals(1, updated);
        assertEquals(List.of("NewProject"), documentProjects(index));
    }

    @Test
    public void removeProjectFromImagesShouldDropTheName() {
        Index index = mockWriteIndex(hitWithProjects(11L, "A", "B"));

        int updated = meiliSearchService.removeProjectFromImages(List.of(11L), "A");

        assertEquals(1, updated);
        assertEquals(List.of("B"), documentProjects(index));
    }

    @Test
    public void removeProjectFromImagesShouldNotRewriteWhenAbsent() {
        Index index = mockWriteIndex(hitWithProjects(11L, "B"));

        int updated = meiliSearchService.removeProjectFromImages(List.of(11L), "A");

        assertEquals(0, updated);
        verify(index, never()).addDocuments(anyString());
    }

    @Test
    public void renameProjectInImagesShouldReplaceNameInPlace() {
        Index index = mockWriteIndex(hitWithProjects(11L, "Old", "Other"));

        int updated = meiliSearchService.renameProjectInImages("Old", "New");

        assertEquals(1, updated);
        assertEquals(List.of("New", "Other"), documentProjects(index));
        assertFalse(captureSearchFilter(index).contains("dataset.alias"));
    }

    @Test
    public void renameProjectInImagesShouldNotDuplicateWhenNewNamePresent() {
        Index index = mockWriteIndex(hitWithProjects(11L, "Old", "New"));

        int updated = meiliSearchService.renameProjectInImages("Old", "New");

        assertEquals(1, updated);
        assertEquals(List.of("New"), documentProjects(index));
    }
}
