package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
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

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.UpdateTerm;

import static be.cytomine.common.repository.http.TermHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermHttpContract {
    String ROOT_PATH = "/terms";

    @GetExchange("/{id}")
    Optional<TermResponse> findTermByID(@PathVariable long id, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId,
                                         @Valid @RequestBody CreateTerm createTerm);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
                                         @RequestParam long userId,
                                         @RequestBody UpdateTerm updateTerm);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id,
                                         @RequestParam long userId);

    @GetExchange("/project/{id}")
    Page<TermResponse> findTermsByProject(@PathVariable long id, @RequestParam long userId, Pageable pageable);

    @GetExchange("/ontology/{id}")
    Page<TermResponse> findTermsByOntology(@PathVariable long id, @RequestParam long userId, Pageable pageable);
}
