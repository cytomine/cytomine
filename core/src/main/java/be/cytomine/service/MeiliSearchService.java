package be.cytomine.service;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Searchable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.cytomine.dto.meilisearch.MeiliSearchFacetsResponse;
import be.cytomine.dto.meilisearch.MeiliSearchImageResponse;
import be.cytomine.exceptions.SearchException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeiliSearchService {

    @Value("${meilisearch.index_id}")
    private String indexId;

    private final Client meiliSearchClient;
    private final ObjectMapper objectMapper;

    public List<MeiliSearchImageResponse> search(
        String query,
        List<String> filters,
        int limit,
        int offset) {

        Index index = doesIndexExist(indexId);

        try {

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
            return result.getHits().stream()
                .map(hit -> objectMapper.convertValue(hit, MeiliSearchImageResponse.class))
                .collect(Collectors.toList());

        } catch (Exception e) {
            throw new SearchException("search failed", 500, e.getMessage());
        }
    }

    public MeiliSearchImageResponse getImage(String imageId) {

        Index index = doesIndexExist(indexId);
        try {

            Object document = index.getDocument(imageId, Object.class);
            return objectMapper.convertValue(document, MeiliSearchImageResponse.class);

        } catch (Exception e) {
            throw new SearchException("MeiliSearch getDocument failed", 500, e.getMessage());
        }
    }


    public MeiliSearchFacetsResponse getFacetDistribution() {

        Index index = doesIndexExist(indexId);
        try {
            String[] attributes = index.getFilterableAttributesSettings();
            SearchRequest searchRequest = new SearchRequest("")
                .setFacets(attributes)
                .setLimit(0);

            SearchResult result = (SearchResult) index.search(searchRequest);
            return objectMapper.convertValue(result.getFacetDistribution(), MeiliSearchFacetsResponse.class);
        } catch (Exception e) {
            throw new SearchException("MeiliSearch getFacetDistribution failed", 500, e.getMessage());
        }
    }

    public Index doesIndexExist(String indexUid) {

        Index[] indexes = meiliSearchClient.getIndexes().getResults();
        for (Index index : indexes) {
            if (indexUid.equals(index.getUid())) {
                return index;
            }
        }
        throw new SearchException("MeiliSearch index not found", 404, "index not found");

    }

}