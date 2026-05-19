package be.cytomine.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.config.properties.ApplicationProperties;

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
}
