package be.cytomine.controller.meta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.domain.CytomineDomain;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.service.command.TransactionService;
import be.cytomine.service.meta.DescriptionService;
import be.cytomine.utils.JsonObject;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestDescriptionController extends RestCytomineController {

    private final DescriptionService descriptionService;

    private final TransactionService transactionService;


    @GetMapping("/description.json")
    public ResponseEntity<String> list() {
        log.debug("REST request to list description");
        return responseSuccess(descriptionService.list());
    }

    @GetMapping("/domain/{domainClassName}/{domainIdent}/description.json")
    public ResponseEntity<String> readByDomain(
        @PathVariable String domainClassName,
        @PathVariable Long domainIdent
    ) {
        log.debug("REST request to read description for domain {} {}", domainClassName,
            domainIdent);

        return responseSuccess(descriptionService.findByDomain(domainClassName.replaceAll("_",
                "."), domainIdent)
            .orElseThrow(() -> new ObjectNotFoundException("Description", JsonObject.of(
                "domainIdent", domainIdent, "domainClassName", domainClassName.replaceAll("_",
                    ".")).toJsonString())));
    }

    @PostMapping({"/description.json", "/domain/{domainClassName}/{domainIdent}/description.json"})
    public ResponseEntity<String> add(
        @PathVariable(required = false) String domainClassName,
        @PathVariable(required = false) Long domainIdent,
        @RequestBody JsonObject json) {
        log.debug("REST request to save description : " + json);
        return add(descriptionService, json);
    }

    @PutMapping("/domain/{domainClassName}/{domainIdent}/description.json")
    public ResponseEntity<String> edit(
        @PathVariable(required = false) String domainClassName,
        @PathVariable(required = false) Long domainIdent,
        @RequestBody JsonObject json
    ) {
        log.debug("REST request to edit description : " + domainClassName + " " + domainIdent);
        return update(descriptionService, json);
    }

    @DeleteMapping("/domain/{domainClassName}/{domainIdent}/description.json")
    public ResponseEntity<String> delete(
        @PathVariable String domainClassName,
        @PathVariable Long domainIdent) {
        log.debug("REST request to delete description for {} {}", domainClassName, domainIdent);
        JsonObject jsonObject = JsonObject.of("domainIdent", domainIdent, "domainClassName",
            domainClassName.replaceAll("_", "."));
        CytomineDomain domain = descriptionService.retrieve(jsonObject);
        return responseSuccess(descriptionService.delete(domain, transactionService.start(), null
            , true));
    }
}
