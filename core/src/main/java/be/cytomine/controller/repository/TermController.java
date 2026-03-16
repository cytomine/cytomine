package be.cytomine.controller.repository;

import java.util.Optional;
import java.util.Set;

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

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.Term;
import be.cytomine.common.repository.model.UpdateTerm;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TermController {

    private final TermHttpContract termHttpContract;

    @GetMapping("term.json")
    public Set<Term> list() {
        log.debug("REST request to list terms");
        return termHttpContract.findAll();
    }

    @PostMapping("term.json")
    public Term create(@RequestBody CreateTerm createTerm) {
        return termHttpContract.update(createTerm);
    }

    @GetMapping("term/{id}.json")
    public Optional<Term> term(@PathVariable long id) {
        log.debug("REST request to get term {}", id);
        return termHttpContract.findTermByID(id);
    }

    @PutMapping("term/{id}.json")
    public Term update(@PathVariable Long id, @RequestBody UpdateTerm updateTerm) {
        return termHttpContract.update(id, updateTerm);
    }

    @DeleteMapping("term/{id}.json")
    public Optional<Term> delete(@PathVariable Long id) {
        log.debug("REST request to delete term {}", id);
        return termHttpContract.delete(id);
    }

    @GetMapping("project/{id}/term.json")
    public Set<Term> listByProject(@PathVariable Long id) {
        log.debug("REST request to list terms for project {}", id);
        return termHttpContract.findTermsByProject(id);
    }

    @GetMapping("ontology/{id}/term.json")
    public Set<Term> listByOntology(@PathVariable Long id) {
        log.debug("REST request to list terms for ontology {}", id);
        return termHttpContract.findTermsByOntology(id);
    }
}
