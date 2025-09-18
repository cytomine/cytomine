package be.cytomine.controller.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.repository.processing.ImageFilterRepository;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestImageFilterController extends RestCytomineController {

    private final ImageFilterRepository imageFilterRepository;

    @GetMapping("/imagefilter.json")
    public ResponseEntity<String> list(
    ) {
        log.debug("REST request to list imagefilterproject");
        return responseSuccess(imageFilterRepository.findAll());
    }
}
