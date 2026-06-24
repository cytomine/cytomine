package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.TagDomainAssociationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TagDomainAssociationController {

    public static final String UNABLE_TO_FIND_TDA = "Unable to find tag domain association with id: %s";

    private final CurrentUserService currentUserService;
    private final PageMapper pageMapper;
    private final TagDomainAssociationHttpContract httpContract;

    @GetMapping("/tag_domain_association.json")
    public CollectionResponse<TagDomainAssociationResponse> readAll(
        @RequestParam long userId,
        Pageable pageable
    ) {
        log.debug("GET /tag_domain_association.json");
        return pageMapper.toCollectionResponse(httpContract.readAll(userId, pageable));
    }

    @GetMapping("/domain/{domainClassName}/{domainId}/tag_domain_association.json")
    public CollectionResponse<TagDomainAssociationResponse> listByDomain(
        @PathVariable String domainClassName,
        @PathVariable long domainId,
        @RequestParam long userId,
        Pageable pageable
    ) {
        log.debug("GET /domain/{}/{}/tag_domain_association.json", domainClassName, domainId);
        return pageMapper.toCollectionResponse(
            httpContract.readAllByDomain(
                domainClassName,
                domainId,
                userId,
                pageable
            )
        );
    }

    @PostMapping({
        "/tag_domain_association.json",
        "/domain/{domainClassName}/{domainId}/tag_domain_association.json"
    })
    public Optional<HttpCommandResponse> create(@RequestBody CreateTagDomainAssociation payload) {
        log.debug("POST /tag_domain_association.json - {}", payload);
        long userId = currentUserService.getCurrentUser().getId();
        return httpContract.create(userId, payload);
    }

    @GetMapping("/tag_domain_association/{id}.json")
    TagDomainAssociationResponse read(@PathVariable long id) {
        log.debug("GET /tag_domain_association/{}.json", id);
        long userId = currentUserService.getCurrentUser().getId();
        return httpContract.read(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TDA, id)));
    }

    @DeleteMapping("/tag_domain_association/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /tag_domain_association/{}.json", id);
        long userId = currentUserService.getCurrentUser().getId();
        return httpContract.delete(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TDA, id)));
    }
}
