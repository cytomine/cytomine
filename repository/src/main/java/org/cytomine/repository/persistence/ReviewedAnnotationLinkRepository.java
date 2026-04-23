package org.cytomine.repository.persistence;

import java.util.Set;

import org.cytomine.repository.persistence.entity.ReviewedAnnotationLinkEntity;
import org.cytomine.repository.persistence.entity.key.ReviewedAnnotationLinkEntityKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewedAnnotationLinkRepository
    extends JpaRepository<ReviewedAnnotationLinkEntity, ReviewedAnnotationLinkEntityKey> {

    Set<ReviewedAnnotationLinkEntity> findAllByReviewedAnnotationTermsId(Long id);

    Set<ReviewedAnnotationLinkEntity> deleteAllByReviewedAnnotationTermsId(Long id);
}
