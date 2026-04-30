package be.cytomine.service.annotation;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.project.Project;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnotationReportService {

    private final ProjectService projectService;

    private final TermService termService;

    private final AnnotationListingBuilder annotationListingBuilder;

    public byte[] downloadDocumentByProject(JsonObject params, Project project) {

        Long idProject = params.getJSONAttrLong("project");
        boolean reviewed = params.getJSONAttrBoolean("reviewed", false);

        String usersParamName = reviewed ? "reviewUsers" : "users";
        Optional<String> requestedUsers = Optional.ofNullable(params.getJSONAttrStr(usersParamName));

        String terms = params.getJSONAttrStr("terms");
        String format = params.getJSONAttrStr("format");

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
