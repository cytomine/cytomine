package org.cytomine.repository.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "annotation_term")
@Getter
@Setter
public class AnnotationTermEntity {
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
    @Column(name = "user_annotation_id")
    private long userAnnotationId;
    @Column(name = "term_id")
    private long termId;
    @Column(name = "user_id")
    private long userId;
}
