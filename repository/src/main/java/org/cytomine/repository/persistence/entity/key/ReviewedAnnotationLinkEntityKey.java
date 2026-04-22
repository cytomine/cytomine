package org.cytomine.repository.persistence.entity.key;

import lombok.Value;


@Value
public class ReviewedAnnotationLinkEntityKey {

    long termId;
    long reviewedAnnotationTermsId;
}
