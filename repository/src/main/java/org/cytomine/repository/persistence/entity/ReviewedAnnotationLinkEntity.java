package org.cytomine.repository.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cytomine.repository.persistence.entity.key.ReviewedAnnotationLinkEntityKey;

@Entity(name = "reviewed_annotation_term")
@IdClass(ReviewedAnnotationLinkEntityKey.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewedAnnotationLinkEntity {
    @Id
    private long termId;
    @Id
    private long reviewedAnnotationTermsId;
}
