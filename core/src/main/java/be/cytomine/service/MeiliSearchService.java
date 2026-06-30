package be.cytomine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Searchable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.cytomine.exceptions.SearchException;


@Service
@RequiredArgsConstructor
public class MeiliSearchService {

    @Value("${meilisearch.index_id}")
    private String indexId;

    private final Client meiliSearchClient;

    public ArrayList<HashMap<String, Object>> search(
        String query,
        List<String> filters,
        int limit,
        int offset) {

        try {
            Index index = meiliSearchClient.index(indexId);

            // Build the filter string exactly as in the original
            String meiliFilter = null;
            if (filters != null && !filters.isEmpty()) {
                String joined = filters.stream()
                    .filter(f -> f != null && !f.trim().isEmpty())
                    .collect(Collectors.joining(" AND "));
                if (!joined.isEmpty()) {
                    meiliFilter = joined;
                }
            }

            // Build and execute the search request
            SearchRequest searchRequest = new SearchRequest(query != null ? query : "")
                .setLimit(limit)
                .setOffset(offset);

            if (meiliFilter != null) {
                searchRequest.setFilter(new String[]{meiliFilter});
            }

            Searchable result = index.search(searchRequest);

            // Extract hits
            return result.getHits();

        } catch (Exception e) {
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    public Map getImage(String imageId) {
        try {
            Index index = meiliSearchClient.index(indexId); // 'client' and 'indexId' are class fields

            // Fetch the document as a Map (raw JSON structure)
            // The SDK automatically deserializes the JSON into the provided class type
            return index.getDocument(imageId, Map.class);

        } catch (Exception e) {
            // Catch-all for network timeouts, serialization issues, etc.
            throw new SearchException("MeiliSearch getDocument failed", 500, e.getMessage());
        }
    }


    public Object getFacetDistribution() {

        Index index = meiliSearchClient.index(indexId);

        String[] attributes = index.getFilterableAttributesSettings();
        // Build the search request
        SearchRequest searchRequest = new SearchRequest("")
            .setFacets(attributes)
            .setLimit(0); // Only facet distribution, no hits

        // Execute the search
        SearchResult result = (SearchResult) index.search(searchRequest);

        // Extract facet distribution
        return result.getFacetDistribution();
    }
}