package be.cytomine.common.repository.http;

import java.util.Optional;
import java.util.Set;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.Term;
import be.cytomine.common.repository.model.UpdateTerm;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermHttpContract {
    String ROOT_PATH = "/terms";

    @GetExchange("/{id}")
    Optional<Term> findTermByID(@PathVariable Long id);

    @GetExchange
    Set<Term> findAll();

    @PostExchange
    Term update(@RequestBody CreateTerm createTerm);

    @PutExchange("/{id}")
    Term update(@PathVariable Long id, @RequestBody UpdateTerm createTerm);

    @DeleteExchange("/{id}")
    Optional<Term> delete(@PathVariable Long id);

    @GetExchange("/project/{id}")
    Set<Term> findTermsByProject(@PathVariable Long id);

    @GetExchange("/ontology/{id}")
    Set<Term> findTermsByOntology(@PathVariable Long id);
}
