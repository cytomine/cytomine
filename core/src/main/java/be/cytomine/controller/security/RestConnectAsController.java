package be.cytomine.controller.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.dto.security.ConnectAsExitResponse;
import be.cytomine.dto.security.ConnectAsResponse;
import be.cytomine.service.security.ConnectAsService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestConnectAsController {

    private final ConnectAsService connectAsService;

    @PostMapping("/user/{id}/connect-as.json")
    public ConnectAsResponse connectAs(@PathVariable Long id) {
        log.debug("REST request to connect as user {}", id);
        return connectAsService.connectAs(id);
    }

    @PostMapping("/user/{id}/connect-as/exit.json")
    public ConnectAsExitResponse exitConnectAs(@PathVariable Long id) {
        log.debug("REST request to exit connect-as session for user {}", id);
        connectAsService.exit(id);
        return new ConnectAsExitResponse(true);
    }
}
