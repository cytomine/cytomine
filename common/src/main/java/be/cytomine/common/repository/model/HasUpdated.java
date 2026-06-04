package be.cytomine.common.repository.model;

import java.sql.Timestamp;

public interface HasUpdated {
    void setUpdated(Timestamp updated);
}
