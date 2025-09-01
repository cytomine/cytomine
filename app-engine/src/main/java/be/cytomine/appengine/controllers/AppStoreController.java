package be.cytomine.appengine.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import be.cytomine.appengine.exceptions.AppStoreServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import be.cytomine.appengine.dto.appstore.InstallRequest;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.exceptions.BundleArchiveException;
import be.cytomine.appengine.exceptions.TaskServiceException;
import be.cytomine.appengine.exceptions.ValidationException;
import be.cytomine.appengine.services.AppStoreService;
import be.cytomine.appengine.services.TaskService;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "${app-engine.api_prefix}${app-engine.api_version}/")
public class AppStoreController {

    private final AppStoreService appStoreService;

    @PostMapping("tasks/{scheme}/{host}/{namespace}/{version}")
    public ResponseEntity<?> install(
        @PathVariable String host,
        @PathVariable String namespace,
        @PathVariable String version,
        @PathVariable String scheme)
            throws IOException,
            TaskServiceException,
            ValidationException,
            BundleArchiveException,
            AppStoreServiceException {
        log.info("tasks/{scheme}/{host}/{namespace}/{version} POST");
        InstallRequest request = new InstallRequest(scheme, host, namespace, version);
        Optional<TaskDescription> description = appStoreService.install(request);
        log.info("tasks/{scheme}/{host}/{namespace}/{version} POST end");
        return ResponseEntity.ok(description);
    }
}
