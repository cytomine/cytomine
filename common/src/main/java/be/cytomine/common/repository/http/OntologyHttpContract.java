package be.cytomine.common.repository.http;

import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;

import static be.cytomine.common.repository.http.OntologyHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface OntologyHttpContract {
    String ROOT_PATH = "/ontologies";


    @GetExchange("/{id}")
    Optional<OntologyResponse> findOntologyByID(@PathVariable long id, @RequestParam long userId);

}
