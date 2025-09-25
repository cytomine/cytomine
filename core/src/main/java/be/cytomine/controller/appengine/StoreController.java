package be.cytomine.controller.appengine;

import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import be.cytomine.dto.appengine.store.AppStore;
import be.cytomine.service.appengine.AppEngineService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final AppEngineService appEngineService;

    private final RestTemplate restTemplate;

    @GetMapping
    public String get() {
        return appEngineService.get("stores");
    }

    @PostMapping
    public String post(@RequestBody AppStore store) {
        return appEngineService.post("stores", store, MediaType.APPLICATION_JSON);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        appEngineService.delete("stores/" + id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Void> setDefaultStore(@PathVariable String id) {
        appEngineService.put("stores/" + id + "/default", null, null);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks")
    public String getStoreTasks(@RequestParam String host) {
        String url = UriComponentsBuilder
                .fromUriString(UriUtils.decode(host, StandardCharsets.UTF_8))
                .pathSegment("api", "v1", "tasks")
                .toUriString();

        return restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();
    }
}
