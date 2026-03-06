package org.cytomine.common.repository.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/ping")
public interface HealthService {

    @GetMapping()
    String ping();
}
