package be.cytomine.controller.report;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.domain.project.Project;
import be.cytomine.dto.annotation.AnnotationReportParams;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.service.annotation.AnnotationReportService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.report.ReportService;
import be.cytomine.utils.JsonNodeUtils;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.ReportType;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ReportController {

    private final AnnotationReportService annotationReportService;

    private final ProjectService projectService;

    private final ReportService reportService;

    @PostMapping("/project/{projectId}/annotation/download")
    public ResponseEntity<byte[]> download(@PathVariable Long projectId, @RequestBody AnnotationReportParams params) {
        ReportType reportType = ReportType.fromLabel(
            (params.format() == null || params.format().isBlank()) ? "pdf" : params.format()
        );

        String users = JsonNodeUtils.csvFromStringList(params.users());
        String reviewUsers = JsonNodeUtils.csvFromStringList(params.reviewUsers());
        String terms = JsonNodeUtils.csvFromStringList(params.terms());
        String images = JsonNodeUtils.csvFromStringList(params.images());
        Long beforeThan = params.beforeThan();
        Long afterThan = params.afterThan();

        Project project = projectService.find(projectId)
            .orElseThrow(() -> new ObjectNotFoundException("Project", projectId));

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("project", projectId);
        bodyMap.put("format", reportType.getLabel());
        bodyMap.put("users", users);
        bodyMap.put("reviewUsers", reviewUsers);
        bodyMap.put("reviewed", params.reviewed());
        bodyMap.put("terms", terms);
        bodyMap.put("images", images);
        bodyMap.put("beforeThan", beforeThan);
        bodyMap.put("afterThan", afterThan);

        JsonObject parameters = new JsonObject(bodyMap);
        byte[] report = annotationReportService.downloadDocumentByProject(parameters, project);
        String filename = reportService.getAnnotationReportFileName(reportType.getLabel(), project.getName());

        return buildReportResponse(filename, report, reportType);
    }

    private ResponseEntity<byte[]> buildReportResponse(String filename, byte[] content, ReportType reportType) {
        MediaType mediaType = switch (reportType) {
            case PDF -> MediaType.APPLICATION_PDF;
            case CSV -> MediaType.parseMediaType("text/csv");
            case EXCEL -> MediaType.APPLICATION_OCTET_STREAM;
        };

        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(content.length)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .body(content);
    }
}
