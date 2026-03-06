package org.cytomine.repository.http;

import org.cytomine.common.repository.http.HealthService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthService {

    @Override
    public String ping() {
        return "pong";
    }
}
