package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "relation")
@Getter
@Setter
public class RelationEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private long version;
    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
    @Column
    private String name;

}
