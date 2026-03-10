package org.cytomine.repository.http;

import org.cytomine.common.repository.http.HealthHttpContract;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(HealthHttpContract.ROOT_PATH)
public class HealthController implements HealthHttpContract {

    @Override
    @GetMapping
    public String ping() {
        return "pong";
    }
}
