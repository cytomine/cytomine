package be.cytomine.common.repository.http;

import java.util.Optional;

import be.cytomine.common.repository.model.OntologyResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(OntologyHttpContract.ROOT_PATH)
public interface OntologyHttpContract {
    String ROOT_PATH = "/ontologies";

    @GetExchange("/by-name/{name}")
    Optional<OntologyResponse> findOntologyByName(@PathVariable String name);

    @GetExchange("/by-id/{id}")
    Optional<OntologyResponse> findOntologyById(@PathVariable long id);
}
