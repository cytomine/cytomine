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

            String meiliFilter = null;
            if (filters != null && !filters.isEmpty()) {
                String joined = filters.stream()
                    .filter(f -> f != null && !f.trim().isEmpty())
                    .collect(Collectors.joining(" AND "));
                if (!joined.isEmpty()) {
                    meiliFilter = joined;
                }
            }

            SearchRequest searchRequest = new SearchRequest(query != null ? query : "")
                .setLimit(limit)
                .setOffset(offset);

            if (meiliFilter != null) {
                searchRequest.setFilter(new String[]{meiliFilter});
            }

            Searchable result = index.search(searchRequest);

            return result.getHits();

        } catch (Exception e) {
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    public Map getImage(String imageId) {
        try {

            Index index = meiliSearchClient.index(indexId);
            return index.getDocument(imageId, Map.class);

        } catch (Exception e) {
            throw new SearchException("MeiliSearch getDocument failed", 500, e.getMessage());
        }
    }


    public Object getFacetDistribution() {

        Index index = meiliSearchClient.index(indexId);

        String[] attributes = index.getFilterableAttributesSettings();
        SearchRequest searchRequest = new SearchRequest("")
            .setFacets(attributes)
            .setLimit(0); // Only facet distribution, no hits

        SearchResult result = (SearchResult) index.search(searchRequest);

        return result.getFacetDistribution();
    }
}