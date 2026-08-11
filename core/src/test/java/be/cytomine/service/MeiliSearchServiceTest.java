package be.cytomine.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.Results;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @InjectMocks
    private MeiliSearchService meiliSearchService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(meiliSearchService, "indexId", INDEX_ID);
    }

    private void mockExistingIndexes(Index... indexes) {
        Results<Index> results = mock();
        when(results.getResults()).thenReturn(indexes);
        when(meiliSearchClient.getIndexes()).thenReturn(results);
    }

    @Test
    public void createIndexIfNotExistsShouldCreateIndexWhenMissing() {
        mockExistingIndexes();

        meiliSearchService.createIndexIfNotExists();

        verify(meiliSearchClient, times(1)).createIndex(INDEX_ID);
    }

    @Test
    public void createIndexIfNotExistsShouldNotCreateIndexWhenAlreadyPresent() {
        Index existing = mock(Index.class);
        when(existing.getUid()).thenReturn(INDEX_ID);
        mockExistingIndexes(existing);

        meiliSearchService.createIndexIfNotExists();

        verify(meiliSearchClient, never()).createIndex(INDEX_ID);
    }

    @Test
    public void createIndexIfNotExistsShouldSwallowFailures() {
        when(meiliSearchClient.getIndexes()).thenThrow(new RuntimeException("MeiliSearch unreachable"));

        assertDoesNotThrow(() -> meiliSearchService.createIndexIfNotExists());

        verify(meiliSearchClient, never()).createIndex(INDEX_ID);
    }
}
