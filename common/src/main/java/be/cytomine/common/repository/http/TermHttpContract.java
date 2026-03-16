package be.cytomine.common.repository.http;

import java.util.Optional;
import java.util.Set;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.Term;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermHttpContract {
    String ROOT_PATH = "/terms";

    @GetExchange("/{id}")
    Optional<Term> findTermByID(@PathVariable Long id);

    @GetExchange
    Set<Term> findAll();

    @PostExchange
    Term add(@RequestBody CreateTerm createTerm);
}
