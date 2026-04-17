package org.cytomine.repository.persistence.projection;

public interface StatUserTermProjection {
    long getUserId();

    String getUsername();

    long getTermId();

    String getTermName();

    String getTermColor();

    long getTermCount();
}
