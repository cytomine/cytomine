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
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.OntologyLight;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@HttpExchange(OntologyHttpContract.ROOT_PATH)
public interface OntologyHttpContract {
    String ROOT_PATH = "/ontologies";

    @GetExchange("/{id}")
    Optional<OntologyResponse> get(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/{id}/light")
    Optional<OntologyLight> getLight(@PathVariable long id, @RequestParam long userId);

    @PostExchange
    Optional<HttpCommandResponse> create(@RequestParam long userId, @Valid @RequestBody CreateOntology createPayload);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(@PathVariable long id, @RequestParam long userId,
        @RequestBody UpdateOntology updateOntology);

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id, @RequestParam long userId);

    @GetExchange("/all-light")
    Page<OntologyLight> getAllLightForUser(@RequestParam long userId, Pageable pageable);

    @GetExchange("/all")
    Page<OntologyResponse> getAllForUser(@RequestParam long userId, Pageable pageable);

}
