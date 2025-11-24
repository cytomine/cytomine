package be.cytomine.controller.appengine;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.service.appengine.AppEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/app-engine") // Defined an "app-engine" prefix to avoid clash with existing Task Controller.
@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("${application.appEngine.enabled: false}")
public class RestTaskController extends RestCytomineController {

    private final AppEngineService appEngineService;

    private final RestTemplate restTemplate;

    @GetMapping("/tasks/{id}")
    public String descriptionById(@PathVariable String id) {
        return appEngineService.get("tasks/" + id);
    }

    @GetMapping("/tasks/{namespace}/{version}")
    public String description(
            @PathVariable String namespace,
            @PathVariable String version,
        @RequestParam(required = false, name = "host") Optional<String> maybeHost
    ) {
        log.info("GET /tasks/{}/{}?host={}", namespace, version, maybeHost);
        return maybeHost
            .map(host -> UriComponentsBuilder
                .fromUriString(UriUtils.decode(host, StandardCharsets.UTF_8))
                .pathSegment("api", "v1", "tasks", namespace, version)
                .toUriString())
            .map(url ->
                restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody())
            .orElseGet(() -> appEngineService.get("tasks/" + namespace + "/" + version));
    }

    @DeleteMapping("/tasks/{namespace}/{version}")
    public void deleteTask(@PathVariable String namespace, @PathVariable String version) {
        log.info("DELETE /tasks/{}/{}", namespace, version);
        appEngineService.delete("tasks/" + namespace + "/" + version);
    }

    @PostMapping("/tasks/{namespace}/{version}/install")
    public String install(
        @PathVariable String namespace,
        @PathVariable String version
    ) {
        String uri = "tasks/" + namespace + "/" + version + "/install";
        return appEngineService.post(uri, null, null);
    }

    @GetMapping("/tasks")
    public String list() {
        return appEngineService.get("tasks");
    }

    @PostMapping("/tasks")
    public String upload(
            @RequestParam("task") MultipartFile task
    ) throws IOException {
        String name = UUID.randomUUID().toString();
        File tmpFile = Files.createTempFile(name, null).toFile();
        task.transferTo(tmpFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("task", new FileSystemResource(tmpFile));
        return appEngineService.post("tasks", body, MediaType.MULTIPART_FORM_DATA);
    }

    @GetMapping("/tasks/{id}/inputs")
    public String inputsById(
            @PathVariable UUID id
    ) {
        return appEngineService.get("tasks/" + id + "/inputs");
    }

    @GetMapping("/tasks/{namespace}/{version}/inputs")
    public String inputs(
            @PathVariable String namespace,
            @PathVariable String version
    ) {
        return appEngineService.get("tasks/" + namespace + "/" + version + "/inputs");
    }

    @GetMapping("/tasks/{id}/outputs")
    public String outputsById(
            @PathVariable UUID id
    ) {
        return appEngineService.get("tasks/" + id + "/outputs");
    }

    @GetMapping("/tasks/{namespace}/{version}/outputs")
    public String outputs(
            @PathVariable String namespace,
            @PathVariable String version
    ) {
        return appEngineService.get("tasks/" + namespace + "/" + version + "/outputs");
    }

    @GetMapping("/tasks/{id}/descriptor.yml")
    public String descriptorById(
            @PathVariable UUID id
    ) {
        return appEngineService.get("tasks/" + id + "/descriptor.yml");
    }

    @GetMapping("/tasks/{namespace}/{version}/descriptor.yml")
    public String descriptor(
            @PathVariable String namespace,
            @PathVariable String version
    ) {
        return appEngineService.get("tasks/" + namespace + "/" + version + "/descriptor.yml");
    }
}
