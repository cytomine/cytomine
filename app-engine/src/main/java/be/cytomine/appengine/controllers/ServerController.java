package be.cytomine.appengine.controllers;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.appengine.dto.responses.PingResponse;

@RestController
public class ServerController {

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse("UP", Instant.now().toString(), "app-engine");
    }
}
