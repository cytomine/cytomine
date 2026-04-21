package org.cytomine.repository.persistence.projection;

public interface StatPerTermAndImageProjection {
    long getImageId();

    long getTermId();

    long getCountAnnotations();
}
