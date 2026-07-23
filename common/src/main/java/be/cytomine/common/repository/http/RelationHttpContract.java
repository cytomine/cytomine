package be.cytomine.common.repository.http;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import be.cytomine.common.repository.model.command.payload.response.RelationResponse;

@HttpExchange(RelationHttpContract.ROOT_PATH)
public interface RelationHttpContract {
    String ROOT_PATH = "/relation";

    @GetExchange("/parent")
    RelationResponse findParentRelation();
}
