package be.cytomine.dto.meilisearch;

import java.util.List;

public record SearchWindow(List<Long> abstractImageIds, long totalHits) {
}