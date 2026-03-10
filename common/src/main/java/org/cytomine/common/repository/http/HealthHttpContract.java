package org.cytomine.common.repository.http;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import static org.cytomine.common.repository.http.HealthHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface HealthHttpContract {

    String ROOT_PATH = "/ping";

    @GetExchange
    String ping();
}
