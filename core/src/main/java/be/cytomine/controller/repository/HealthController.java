package be.cytomine.controller.repository;

import org.cytomine.common.repository.http.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/repository/ping")
public class HealthController {
    HealthService healthService;

    @GetMapping
    public String ping() {
        return healthService.ping();

    }

}
