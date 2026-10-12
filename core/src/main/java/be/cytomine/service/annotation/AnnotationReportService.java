package be.cytomine.service.annotation;

import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.domain.project.Project;
import be.cytomine.service.project.ProjectService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.JsonObject;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnotationReportService {

    private final AnnotationListingBuilder annotationListingBuilder;

    private final ProjectService projectService;

    private final TermHttpContract termHttpContract;

    public byte[] downloadDocumentByProject(JsonObject params, Project project) {

        Long idProject = params.getJSONAttrLong("project");
        boolean reviewed = params.getJSONAttrBoolean("reviewed", false);

        String usersParamName = reviewed ? "reviewUsers" : "users";
        Optional<String> requestedUsers = Optional.ofNullable(params.getJSONAttrStr(usersParamName));

        String termsParam = params.getJSONAttrStr("terms");
        String format = params.getJSONAttrStr("format");

        String userIds = requestedUsers
            .filter(s -> !s.isBlank())
            .orElseGet(() -> projectService.getUserIdsFromProject(project.getId()));

        String terms =
            termsParam == null || termsParam.isBlank() ? termHttpContract.findAllTermIdsByProject(idProject)
                .stream().map(String::valueOf).collect(
                    Collectors.joining(",")) :
                termsParam;

        if (reviewed) {
            params.put("reviewed", true);
        }

        log.info("Download report for project {} with users {} and terms {}", idProject, userIds, terms);

        return annotationListingBuilder.buildAnnotationReport(idProject, userIds, params, terms, format);
    }
}
