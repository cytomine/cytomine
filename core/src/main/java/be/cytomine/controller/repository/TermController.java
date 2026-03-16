package be.cytomine.controller.repository;

import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.Term;

@RestController
@RequestMapping("/api/term")
@Slf4j
@RequiredArgsConstructor
public class TermController {

    private final TermHttpContract termHttpContract;

    @GetMapping(".json")
    public Set<Term> list() {
        log.debug("REST request to list terms");
        return termHttpContract.findAll();
    }

    @GetMapping("{id}.json")
    public Optional<Term> term(@PathVariable long id) {
        log.debug("REST request to list terms");
        return termHttpContract.findTermByID(id);
    }

}
