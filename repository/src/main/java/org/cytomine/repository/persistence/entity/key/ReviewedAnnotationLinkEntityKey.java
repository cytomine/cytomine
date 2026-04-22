package org.cytomine.repository.persistence.entity.key;

import lombok.Data;


@Data
public class ReviewedAnnotationLinkEntityKey {

    long termId;
    long reviewedAnnotationTermsId;
}
