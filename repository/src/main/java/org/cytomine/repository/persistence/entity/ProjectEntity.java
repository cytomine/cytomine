package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "project")
@Data
public class ProjectEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @Version
    private long version = 0;

    @Column
    private String name;
    @Column
    private Long ontologyId;
    @Column
    private boolean blindMode = false;
    @Column
    private boolean areImagesDownloadable = false;
    @Column(name = "is_closed")
    private boolean closed = false;
    @Column
    private boolean hideUsersLayers = false;
    @Column
    private boolean hideAdminsLayers = false;
    @Column
    private String mode = "CLASSIC";
    @Column
    private long countAnnotations;
    @Column
    private long countImages;
    @Column
    private long countJobAnnotations;
    @Column
    private long countReviewedAnnotations;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
}
