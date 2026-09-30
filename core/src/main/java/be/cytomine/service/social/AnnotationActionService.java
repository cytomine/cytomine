package be.cytomine.service.social;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.social.AnnotationAction;
import be.cytomine.repositorynosql.social.AnnotationActionRepository;
import be.cytomine.service.database.SequenceService;
import be.cytomine.service.security.SecurityACLService;

import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AnnotationActionService {

    private final SequenceService sequenceService;

    private final AnnotationActionRepository annotationActionRepository;

    private final SecurityACLService securityACLService;

    private final MongoTemplate mongoTemplate;

    public AnnotationAction add(AnnotationDomain annotation, long userId, String action, Date created) {
        securityACLService.check(annotation, READ);
        AnnotationAction annotationAction = new AnnotationAction();
        annotationAction.setId(sequenceService.generateID());
        annotationAction.setUser(userId);
        annotationAction.setImage(annotation.getImage().getId());
        annotationAction.setSlice(annotation.getSlice().getId());
        annotationAction.setProject(annotation.getProject().getId());
        annotationAction.setCreated(created);
        annotationAction.setAction(action);
        annotationAction.setAnnotationIdent(annotation.getId());
        annotationAction.setAnnotationClassName(annotation.getClass().getName());
        annotationAction.setAnnotationCreator(annotation.getUserId());

        return annotationActionRepository.insert(annotationAction);
    }

    public List<AnnotationAction> list(SliceInstance sliceInstance, Optional<Long> userId, Long afterThan,
        Long beforeThan) {
        securityACLService.checkIsAdminContainer(sliceInstance);
        Query query = new Query();
        query.addCriteria(Criteria.where("slice").is(sliceInstance.getId()));
        userId.ifPresent(id -> query.addCriteria(Criteria.where("user").is(id)));
        if (afterThan != null && beforeThan != null) {
            query.addCriteria(Criteria.where("created").gte(new Date(afterThan)).lte(new Date(beforeThan)));
        } else if (afterThan != null) {
            query.addCriteria(Criteria.where("created").gte(new Date(afterThan)));
        } else if (beforeThan != null) {
            query.addCriteria(Criteria.where("created").lte(new Date(beforeThan)));
        }
        query.with(Sort.by(Sort.Direction.ASC, "created"));

        return mongoTemplate.find(query, AnnotationAction.class);
    }

    public List<AnnotationAction> list(ImageInstance imageInstance, Optional<Long> userId, Long afterThan,
        Long beforeThan) {
        securityACLService.checkIsAdminContainer(imageInstance);
        Query query = new Query();
        query.addCriteria(Criteria.where("image").is(imageInstance.getId()));
        userId.ifPresent(id -> query.addCriteria(Criteria.where("user").is(id)));
        if (afterThan != null && beforeThan != null) {
            query.addCriteria(Criteria.where("created").gte(new Date(afterThan)).lte(new Date(beforeThan)));
        } else if (afterThan != null) {
            query.addCriteria(Criteria.where("created").gte(new Date(afterThan)));
        } else if (beforeThan != null) {
            query.addCriteria(Criteria.where("created").lte(new Date(beforeThan)));
        }
        query.with(Sort.by(Sort.Direction.ASC, "created"));

        return mongoTemplate.find(query, AnnotationAction.class);
    }

    public Long countByProject(Project project, Long startDate, Long endDate) {
        if (startDate == null && endDate == null) {
            return annotationActionRepository.countByProject(project.getId());
        } else if (endDate == null) {
            return annotationActionRepository.countByProjectAndCreatedAfter(project.getId(), new Date(startDate));
        } else if (startDate == null) {
            return annotationActionRepository.countByProjectAndCreatedBefore(project.getId(), new Date(endDate));
        } else {
            return annotationActionRepository.countByProjectAndCreatedBetween(
                project.getId(),
                new Date(startDate),
                new Date(endDate)
            );
        }
    }
}
