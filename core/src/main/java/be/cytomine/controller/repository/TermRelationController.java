package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TermRelationController {

    public static final String UNABLE_TO_FIND_TERM_RELATION = "Unable to find term relation with id: %s";
    private final TermRelationHttpContract termRelationHttpContract;
    private final CurrentUserService currentUserService;

    private record CreateTermRelationRequest(long term1, long term2) {}

    @PostMapping({"relation/term.json", "relation/parent/term.json"})
    public Optional<HttpCommandResponse> create(@RequestBody CreateTermRelationRequest request) {
        long userId = currentUserService.getCurrentUser().getId();
        return termRelationHttpContract.create(userId,
            new CreateTermRelation(request.term1(), request.term2(), RelationTerm.PARENT));
    }

    @GetMapping("relation/term/{id}.json")
    public TermRelationResponse termRelation(@PathVariable long id) {
        log.debug("REST request to get term relation {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return termRelationHttpContract.findTermRelationByID(id, userId)
                   .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TERM_RELATION, id)));
    }

    @PutMapping("relation/term/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateTermRelation updateTermRelation) {
        long userId = currentUserService.getCurrentUser().getId();
        return termRelationHttpContract.update(id, userId, updateTermRelation)
                   .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TERM_RELATION, id)));
    }

    @DeleteMapping("relation/term/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete term relation {}", id);
        return termRelationHttpContract.delete(id, currentUserService.getCurrentUser().getId())
                   .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_TERM_RELATION, id)));
    }

    @DeleteMapping("relation/parent/term1/{idTerm1}/term2/{idTerm2}.json")
    public HttpCommandResponse deleteByTerms(@PathVariable Long idTerm1, @PathVariable Long idTerm2) {
        long userId = currentUserService.getCurrentUser().getId();
        return termRelationHttpContract.deleteByTerms(idTerm1, idTerm2, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                format("Unable to find term relation for term1=%s term2=%s", idTerm1, idTerm2)));
    }
}
