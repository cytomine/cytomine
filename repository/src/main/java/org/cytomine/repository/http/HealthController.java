package org.cytomine.repository.http;

import org.cytomine.common.repository.http.HealthService;


public class HealthController implements HealthService {

    @Override
    public String ping() {
        return "pong";
    }
}
