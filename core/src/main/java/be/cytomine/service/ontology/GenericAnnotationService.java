package be.cytomine.service.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.ontology.AnnotationDomainRepository;
import be.cytomine.repository.ontology.ReviewedAnnotationRepository;
import be.cytomine.repository.ontology.UserAnnotationRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.security.SecurityACLService;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class GenericAnnotationService {

    private final EntityManager entityManager;

    private final AnnotationDomainRepository annotationDomainRepository;

    private final CurrentUserService currentUserService;

    private final ReviewedAnnotationRepository reviewedAnnotationRepository;

    private final SecurityACLService securityACLService;

    private final UserAnnotationRepository userAnnotationRepository;

    /**
     * Find all annotation id from a specific table created by a user that touch location geometry
     *
     * @param location WKT Location that must touch result annotation
     * @param idImage  Annotation image
     * @param layers   Annotation Users
     * @param table    Table that store annotation (user, reviewed)
     *
     * @return List of annotation id from idImage and idUser that touch location
     */
    public List<AnnotationDomain> findAnnotationThatTouch(
        String location,
        List<Long> layers,
        long idImage,
        String table
    ) {
        ImageInstance image = entityManager.find(ImageInstance.class, idImage);

        boolean projectAdmin = securityACLService.hasPermission(image.getProject(), ADMINISTRATION);
        if (!projectAdmin) {
            layers = layers.stream().filter(x -> Objects.equals(x, currentUserService.getCurrentUser().id()))
                .collect(Collectors.toList());
        }

        List<Tuple> results;
        if (table.equals("reviewed_annotation")) {
            results = annotationDomainRepository.findAllIntersectForReviewedAnnotations(image.getId(), location);
        } else {
            results = annotationDomainRepository.findAllIntersectForUserAnnotations(image.getId(), layers, location);
        }

        List<Long> ids = new ArrayList<>();
        Set<Long> users = new HashSet<>();
        for (Tuple result : results) {
            ids.add((Long) result.get("annotation"));
            users.add((Long) result.get("user"));
        }

        if (users.size() > 1
            && !table.equals("reviewed_annotation")) { //if more annotation from more than 1 user NOT IN REVIEW MODE!
            throw new WrongArgumentException(
                "Annotations from multiple users are under this area. "
                    + "You can correct only annotation from 1 user (hide layer if necessary)");
        }

        List<AnnotationDomain> annotations = annotationDomainRepository.findAllById(ids);

        Map<Long, Double> termSizes = new HashMap<>();
        for (AnnotationDomain annotation : annotations) {
            for (Long termId : annotation.termsId()) {
                Double currentValue = termSizes.getOrDefault(termId, 0d);
                termSizes.put(termId, currentValue + annotation.getArea());
            }
        }

        Double min = Double.MAX_VALUE;
        Long goodTerm = null;


        if (!termSizes.isEmpty()) {
            for (Map.Entry<Long, Double> entry : termSizes.entrySet()) {
                if (min > entry.getValue()) {
                    min = entry.getValue();
                    goodTerm = entry.getKey();
                }
            }

            for (AnnotationDomain annotation : annotations) {
                if (!annotation.termsId().contains(goodTerm)) {
                    annotations.remove(annotation);
                }
            }
        }

        return annotations.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Find all reviewed annotation domain instance with ids and exactly the same term All these annotation must have
     * this single term
     *
     * @param ids     List of reviewed annotation id
     * @param termsId Term that must have all reviewed annotation (
     *
     * @return Reviewed Annotation list
     */
    public List<ReviewedAnnotation> findReviewedAnnotationWithTerm(List<Long> ids, List<Long> termsId) {
        List<ReviewedAnnotation> annotationsWithSameTerm = new ArrayList<>();
        for (Long id : ids) {
            ReviewedAnnotation compared = reviewedAnnotationRepository.findById(id).get();
            List<Long> idTerms = compared.termsId();
            if (idTerms.size() != termsId.size()) {
                throw new WrongArgumentException("Annotations have not the same term!");
            }

            for (Long idTerm : idTerms) {
                if (!termsId.contains(idTerm)) {
                    throw new WrongArgumentException("Annotations have not the same term!");
                }
            }
            annotationsWithSameTerm.add(compared);
        }
        return annotationsWithSameTerm;
    }

    /**
     * Find all user annotation domain instance with ids and exactly the same term All these annotation must have this
     * single term
     *
     * @param ids     List of user annotation id
     * @param termsId Term that must have all user annotation (
     *
     * @return user Annotation list
     */
    public List<UserAnnotation> findUserAnnotationWithTerm(List<Long> ids, List<Long> termsId) {
        List<UserAnnotation> annotationsWithSameTerm = new ArrayList<>();
        for (Long id : ids) {
            UserAnnotation compared = userAnnotationRepository.findById(id).get();
            List<Long> idTerms = compared.termsId();
            if (idTerms.size() != termsId.size()) {
                throw new WrongArgumentException("Annotations have not the same term!");
            }

            for (Long idTerm : idTerms) {
                if (!termsId.contains(idTerm)) {
                    throw new WrongArgumentException("Annotations have not the same term!");
                }
            }
            annotationsWithSameTerm.add(compared);
        }
        return annotationsWithSameTerm;
    }
}
