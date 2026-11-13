package org.cytomine.common.repository.http;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/ping")
public interface HealthService {

    @GetExchange
    String ping();
}
