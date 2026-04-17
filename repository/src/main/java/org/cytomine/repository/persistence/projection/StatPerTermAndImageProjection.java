package org.cytomine.repository.persistence.projection;

public interface StatPerTermAndImageProjection {
    long getImage();

    long getTerm();

    long getCountAnnotations();
}
