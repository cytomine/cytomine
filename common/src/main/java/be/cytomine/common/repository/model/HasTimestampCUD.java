package be.cytomine.common.repository.model;

import java.sql.Timestamp;

public interface HasTimestampCUD {
    Timestamp getUpdated();

    void setUpdated(Timestamp timestamp);

    Timestamp getDeleted();

    void setDeleted(Timestamp timestamp);

    Timestamp getCreated();

    void setCreated(Timestamp timestamp);
}
