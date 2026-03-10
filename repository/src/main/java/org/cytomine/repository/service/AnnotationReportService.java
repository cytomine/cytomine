package org.cytomine.repository.service;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.common.repository.http.payload.AnnotationReportParams;
import org.cytomine.common.repository.persistence.ProjectEntity;
import org.cytomine.repository.persistence.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AnnotationReportService {
    ProjectRepository projectRepository;

    public byte[] downloadDocumentByProject(long projectID, AnnotationReportParams params) {


        String usersParamName = params.reviewed() ? "reviewUsers" : "users";
        // Optional<String> requestedUsers = Optional.ofNullable(params.users());

        Optional<ProjectEntity> project = projectRepository.findById(projectID);


        // log.info("Download report for project {} with users {} and terms {}", idProject, userIds, terms);

        return null;
    }
}
