package be.cytomine.controller.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.CommandHttpContract;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class CommandV2Controller {
    private final CommandHttpContract commandHttpContract;
}
