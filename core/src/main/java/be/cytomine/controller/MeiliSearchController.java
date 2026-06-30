package be.cytomine.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.service.MeiliSearchService;


@RestController
@RequestMapping("/api/meilisearch")
@RequiredArgsConstructor
public class MeiliSearchController {

    private final MeiliSearchService meiliSearchService;

    @GetMapping("/search")
    public ResponseEntity<ArrayList<HashMap<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(required = false) List<String> filters,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {

        ArrayList<HashMap<String, Object>>
            results = meiliSearchService.search(query, filters, limit, offset);
        return ResponseEntity.ok(results);

    }

    @GetMapping("/image/{imageid}")
    public ResponseEntity<Map<String, Object>> getDocument(
            @PathVariable String imageid) {

        Map document = meiliSearchService.getImage(imageid);
        if (document != null) {
            return ResponseEntity.ok(document);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/facets")
    public ResponseEntity<Object> getFacets() {

        Object facets =  meiliSearchService.getFacetDistribution();
        return ResponseEntity.ok(facets);

    }
}
