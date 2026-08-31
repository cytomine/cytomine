package org.cytomine.repository.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.HealthService;

@RestController
@RequestMapping("/ping")
public class HealthController implements HealthService {

    @Override
    @GetMapping
    public String ping() {
        return "pong";
    }
}
