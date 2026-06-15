package be.cytomine.controller.image.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.image.server.StorageService;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestStorageController extends RestCytomineController {

    private final StorageService storageService;

    private final CurrentUserService currentUserService;

    @GetMapping("/storage.json")
    public ResponseEntity<String> list(
        @RequestParam(defaultValue = "false", required = false) Boolean all
    ) {
        log.debug("REST request to list storages: all? {}", all);
        return responseSuccess(
            all ? storageService.list() : storageService.list(currentUserService.getCurrentUser(), null)
        );
    }
}
