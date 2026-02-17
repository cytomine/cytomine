package be.cytomine.service.annotation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.annotation.AnnotationRepository;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.report.ReportService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnotationReportService {

    private final ProjectService projectService;

    private final TermService termService;

    private final AnnotationListingBuilder annotationListingBuilder;

    private final ReportService reportService;

    public byte[] downloadDocumentByProject(JsonObject params) {

        Long idProject = params.getJSONAttrLong("project");
        boolean reviewed = params.getJSONAttrBoolean("reviewed", false);

        String usersParamName = reviewed ? "reviewUsers" : "users";
        Optional<String> requestedUsers = Optional.ofNullable(params.getJSONAttrStr(usersParamName));

        String terms = params.getJSONAttrStr("terms");
        String format = params.getJSONAttrStr("format");

        Project project = projectService.find(idProject)
            .orElseThrow(() -> new ObjectNotFoundException("Project", idProject));

        String userIds = requestedUsers
            .filter(s -> !s.isBlank())
            .orElseGet(() -> projectService.getUserIdsFromProject(project.getId()));

        terms = termService.fillEmptyTermIds(terms, project);

        if (reviewed) {
            params.put("reviewed", true);
        }

        log.info("Download report for project {} with users {} and terms {}", idProject, userIds, terms);

        return annotationListingBuilder.buildAnnotationReport(idProject, userIds, params, terms, format);
    }

}
