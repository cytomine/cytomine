package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.TermResponse;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TermController {

    public static final String UNABLE_TO_FIND_TERM = "Unable to find term with id: %s";
    private final TermHttpContract termHttpContract;
    private final PageMapper pageMapper;
    private final CurrentUserService currentUserService;

    @PostMapping("term.json")
    public Optional<HttpCommandResponse<TermResponse>> create(@RequestBody CreateTerm createTerm) {
        long userId = currentUserService.getCurrentUser().getId();
        return termHttpContract.create(userId, createTerm);
    }

    @GetMapping("term/{id}.json")
    public TermResponse term(@PathVariable long id) {
        log.debug("REST request to get term {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return termHttpContract.findTermByID(id, userId)
                   .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                       format(UNABLE_TO_FIND_TERM, id)));
    }

    @PutMapping("term/{id}.json")
    public HttpCommandResponse<TermResponse> update(@PathVariable Long id,
                                                    @RequestBody UpdateTerm updateTerm) {
        long userId = currentUserService.getCurrentUser().getId();

        return termHttpContract.update(id, userId, updateTerm).orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
            format(UNABLE_TO_FIND_TERM, id)));
    }

    @DeleteMapping("term/{id}.json")
    public HttpCommandResponse<TermResponse> delete(@PathVariable Long id) {
        log.debug("REST request to delete term {}", id);
        return termHttpContract.delete(id, currentUserService.getCurrentUser().getId())
                   .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                       format(UNABLE_TO_FIND_TERM, id)));
    }

    @GetMapping("project/{id}/term.json")
    public CollectionResponse<TermResponse> listByProject(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to list terms for project {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return pageMapper.toCollectionResponse(termHttpContract.findTermsByProject(id, userId, pageable));
    }

    @GetMapping("ontology/{id}/term.json")
    public CollectionResponse<TermResponse> listByOntology(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to list terms for ontology {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return pageMapper.toCollectionResponse(termHttpContract.findTermsByOntology(id, userId, pageable));
    }
}
