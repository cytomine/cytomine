package be.cytomine.appengine.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.appengine.exceptions.AppStoreNotFoundException;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.models.store.AppStore;
import be.cytomine.appengine.services.AppStoreService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "${app-engine.api_prefix}${app-engine.api_version}/stores")
class AppStoreController {

    private final AppStoreService appStoreService;

    @PostMapping
    public ResponseEntity<AppStore> post(@RequestBody AppStore appStore) throws ValidationException {
        log.info("Store POST");
        AppStore store = appStoreService.save(appStore);
        log.info("Store POST Ended");
        return ResponseEntity.ok(store);
    }

    @GetMapping
    public ResponseEntity<List<AppStore>> get() throws ValidationException {
        log.info("Store GET");
        List<AppStore> store = appStoreService.list();
        log.info("Store GET Ended");
        return ResponseEntity.ok(store);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) throws ValidationException {
        log.info("Store DELETE");
        appStoreService.delete(UUID.fromString(id));
        log.info("Store DELETE Ended");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<?> put(@PathVariable String id) throws AppStoreNotFoundException {
        log.info("Store PUT default");
        appStoreService.makeDefault(UUID.fromString(id));
        log.info("Store PUT default Ended");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/default")
    public ResponseEntity<Optional<AppStore>> findDefault() throws AppStoreNotFoundException {
        log.info("Store Get default");
        Optional<AppStore> store = appStoreService.findDefault();
        log.info("Store Get default Ended");
        return ResponseEntity.ok(store);
    }
    
}