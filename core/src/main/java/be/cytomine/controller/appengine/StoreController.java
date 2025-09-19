package be.cytomine.controller.appengine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.dto.appengine.store.AppStore;
import be.cytomine.service.appengine.AppEngineService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StoreController {

    private final AppEngineService appEngineService;

    @GetMapping("/stores")
    public String get() {
        return appEngineService.get("stores");
    }

    @PostMapping("/stores")
    public String post(@RequestBody AppStore store) {
        return appEngineService.post("stores", store, MediaType.APPLICATION_JSON);
    }

    @DeleteMapping("/stores")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        appEngineService.delete("stores/" + id);
        return ResponseEntity.noContent().build();
    }
}
