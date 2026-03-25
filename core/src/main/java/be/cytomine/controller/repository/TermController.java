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

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.controller.utils.CollectionResponse;
import be.cytomine.controller.utils.PageMapper;
import be.cytomine.service.CurrentUserService;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TermController {

    private final TermHttpContract termHttpContract;
    private final PageMapper pageMapper;
    private final CurrentUserService currentUserService;

    @GetMapping("term.json")
    public CollectionResponse<TermResponse> list(Pageable pageable) {
        log.debug("REST request to list terms");
        return pageMapper.toCollectionResponse(termHttpContract.findAll(pageable));
    }

    @PostMapping("term.json")
    public Optional<HttpCommandResponse<TermResponse>> create(@RequestBody CreateTerm createTerm) {
        long userId = currentUserService.getCurrentUser().getId();
        return termHttpContract.create(userId, createTerm);
    }

    @GetMapping("term/{id}.json")
    public Optional<TermResponse> term(@PathVariable long id) {
        log.debug("REST request to get term {}", id);
        return termHttpContract.findTermByID(id);
    }

    @PutMapping("term/{id}.json")
    public Optional<HttpCommandResponse<TermResponse>> update(@PathVariable Long id,
                                                              @RequestBody UpdateTerm updateTerm) {
        long userId = currentUserService.getCurrentUser().getId();

        return termHttpContract.update(id, userId, updateTerm);
    }

    @DeleteMapping("term/{id}.json")
    public Optional<HttpCommandResponse<TermResponse>> delete(@PathVariable Long id) {
        log.debug("REST request to delete term {}", id);
        return termHttpContract.delete(id, currentUserService.getCurrentUser().getId());
    }

    @GetMapping("project/{id}/term.json")
    public CollectionResponse<TermResponse> listByProject(@PathVariable Long id,
                                                          Pageable pageable) {
        log.debug("REST request to list terms for project {}", id);
        return pageMapper.toCollectionResponse(termHttpContract.findTermsByProject(id, pageable));
    }

    @GetMapping("ontology/{id}/term.json")
    public CollectionResponse<TermResponse> listByOntology(@PathVariable Long id,
                                                           Pageable pageable) {
        log.debug("REST request to list terms for ontology {}", id);
        return pageMapper.toCollectionResponse(termHttpContract.findTermsByOntology(id, pageable));
    }
}
