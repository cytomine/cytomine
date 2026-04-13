package org.cytomine.repository.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "user_annotation")
@Getter
@Setter
public class UserAnnotationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private long version;
    @Column
    private LocalDateTime created;
    @Column
    private LocalDateTime updated;
    @Column
    private LocalDateTime deleted;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "image_id")
    private long imageId;
    @Column(name = "slice_id")
    private long sliceId;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "wkt_location")
    private String wktLocation;
    @Column(name = "geometry_compression")
    private double geometryCompression;
    @Column(name = "count_reviewed_annotations")
    private int countReviewedAnnotations;
}
