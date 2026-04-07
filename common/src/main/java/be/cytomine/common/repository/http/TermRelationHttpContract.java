package be.cytomine.common.repository.http;

import org.springframework.web.service.annotation.HttpExchange;

import static be.cytomine.common.repository.http.TermRelationHttpContract.ROOT_PATH;

@HttpExchange(ROOT_PATH)
public interface TermRelationHttpContract {
    String ROOT_PATH = "/term_relations";

}
