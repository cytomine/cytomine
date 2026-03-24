package be.cytomine.common.repository.http;

import java.util.Optional;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(ROOT_PATH)
public interface TermHttpContract {
    String ROOT_PATH = "/terms";

    @GetExchange("/{id}")
    Optional<TermResponse> findTermByID(@PathVariable Long id);

    @GetExchange
    Page<TermResponse> findAll(Pageable pageable);

    @PostExchange
    TermResponse update(@RequestBody CreateTerm createTerm);

    @PutExchange("/{id}")
    TermResponse update(@PathVariable Long id, @RequestBody UpdateTerm createTerm);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse<TermResponse>> delete(@PathVariable Long id);

    @GetExchange("/project/{id}")
    Page<TermResponse> findTermsByProject(@PathVariable Long id, Pageable pageable);

    @GetExchange("/ontology/{id}")
    Page<TermResponse> findTermsByOntology(@PathVariable Long id, Pageable pageable);
}
