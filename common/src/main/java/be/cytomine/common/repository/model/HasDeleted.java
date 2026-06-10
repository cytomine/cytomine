package be.cytomine.common.repository.model;

import java.sql.Timestamp;

public interface HasDeleted {
    void setDeleted(Timestamp deleted);
}
