package be.cytomine.controller.meta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.service.meta.TagDomainAssociationService;
import be.cytomine.utils.JsonObject;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestTagDomainAssociationController extends RestCytomineController {

    private final TagDomainAssociationService tagDomainAssociationService;

    @PostMapping({
        "/tag_domain_association.json",
        "/domain/{domainClassName}/{domainIdent}/tag_domain_association.json"
    })
    public ResponseEntity<String> add(
        @PathVariable(required = false) String domainClassName,
        @PathVariable(required = false) Long domainIdent,
        @RequestBody JsonObject json
    ) {
        log.debug("REST request to save Tag association: " + json);
        return add(tagDomainAssociationService, json);
    }
}
