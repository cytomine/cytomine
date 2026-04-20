package be.cytomine.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.utils.JsonObject;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("")
@RestController
public class ServerController extends RestCytomineController {

    private final ApplicationProperties applicationProperties;

    @GetMapping("/server/ping")
    public Map<String, Object> ping() {
        log.debug("GET /server/ping");
        return Map.of(
            "alive", true,
            "version", applicationProperties.getVersion(),
            "serverURL", applicationProperties.getServerURL(),
            "serverID", applicationProperties.getServerId()
        );
    }

    @GetMapping("/status.json")
    public ResponseEntity<String> status() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("alive", true);
        jsonObject.put("version", applicationProperties.getVersion());
        jsonObject.put("serverURL", applicationProperties.getServerURL());
        return ResponseEntity.ok(jsonObject.toJsonString());
    }
}
