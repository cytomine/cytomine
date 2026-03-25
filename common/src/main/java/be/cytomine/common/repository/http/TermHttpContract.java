package be.cytomine.common.repository.http;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.HttpCommandResponse;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermHttpContract {
    String ROOT_PATH = "/terms";

    @GetExchange("/{id}")
    Optional<TermResponse> findTermByID(@PathVariable long id);

    @GetExchange
    Page<TermResponse> findAll(Pageable pageable);

    @PostExchange
    Optional<HttpCommandResponse<TermResponse>> create(@RequestParam long userId,
                                                       @RequestBody CreateTerm createTerm);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse<TermResponse>> update(@PathVariable long id,
                                                       @RequestParam long userId,
                                                       @RequestBody UpdateTerm createTerm);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse<TermResponse>> delete(@PathVariable long id,
                                                       @RequestParam long userId);

    @GetExchange("/project/{id}")
    Page<TermResponse> findTermsByProject(@PathVariable long id, Pageable pageable);

    @GetExchange("/ontology/{id}")
    Page<TermResponse> findTermsByOntology(@PathVariable long id, Pageable pageable);
}
