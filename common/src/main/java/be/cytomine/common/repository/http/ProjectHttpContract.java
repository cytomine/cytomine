package be.cytomine.common.repository.http;

import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(ProjectHttpContract.ROOT_PATH)
public interface ProjectHttpContract {
    String ROOT_PATH = "/projects";
}
