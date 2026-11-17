package org.cytomine.common.repository.http;

import org.cytomine.common.repository.model.command.payload.response.RelationResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(RelationHttpContract.ROOT_PATH)
public interface RelationHttpContract {
    String ROOT_PATH = "/relation";

    @GetExchange("/parent")
    RelationResponse findParentRelation();
}
