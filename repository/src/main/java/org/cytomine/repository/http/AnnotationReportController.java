package org.cytomine.repository.http;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.repository.http.AnnotationReportHttpContract;
import org.cytomine.common.repository.http.payload.AnnotationReportParams;
import org.cytomine.common.repository.utils.JsonNodeUtils;
import org.cytomine.repository.service.AnnotationReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(AnnotationReportHttpContract.ROOT_PATH)
public class AnnotationReportController implements AnnotationReportHttpContract {

    private final AnnotationReportService annotationReportService;

    @PostMapping(AnnotationReportHttpContract.DOWNLOAD_PATH)
    @Override
    public ResponseEntity<byte[]> download(Long projectID, AnnotationReportParams params) {

        byte[] report = annotationReportService.downloadDocumentByProject(projectID, params);

        return ResponseEntity.ofNullable(report);
    }
}
