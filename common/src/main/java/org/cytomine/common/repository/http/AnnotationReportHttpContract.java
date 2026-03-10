package org.cytomine.common.repository.http;

import org.cytomine.common.repository.http.payload.AnnotationReportParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(AnnotationReportHttpContract.ROOT_PATH)
public interface AnnotationReportHttpContract {

    String ROOT_PATH = "/annotations/report";
    String DOWNLOAD_PATH = "/project/{project}";

    @PostExchange(DOWNLOAD_PATH)
    ResponseEntity<byte[]> download(@PathVariable Long project, @RequestBody AnnotationReportParams params);
}
