package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;

import static be.cytomine.common.repository.http.TermRelationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermRelationHttpContract {
    String ROOT_PATH = "/term_relations";

    @GetExchange("/{id}")
    Optional<TermRelationResponse> findTermByID(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/ontology/{ontologyId}")
    List<TermRelationResponse> findAllByOntologyId(@PathVariable long ontologyId, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId,
                                         @Valid @RequestBody CreateTermRelation createTermRelation);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id,
                                         @RequestParam long userId,
                                         @RequestBody UpdateTermRelation updateTermRelation);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id,
                                         @RequestParam long userId);
}
