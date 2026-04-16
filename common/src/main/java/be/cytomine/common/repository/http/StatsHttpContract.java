package be.cytomine.common.repository.http;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import be.cytomine.common.repository.model.stat.payload.StatTerm;

import static be.cytomine.common.repository.http.StatsHttpContract.ROOT_PATH;


@HttpExchange(ROOT_PATH)

public interface StatsHttpContract {
    String ROOT_PATH = "/stats";

    @GetExchange("/ontology/{id}")
    Page<StatTerm> findTermsByOntology(@PathVariable long id, @RequestParam long userId, Pageable pageable);

}
