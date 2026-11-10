package be.cytomine.controller.repository;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.repository.http.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repository/ping")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping
    public String ping() {
        return healthService.ping();
    }
}
