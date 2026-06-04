package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.common.repository.model.HasDeleted;
import be.cytomine.common.repository.model.HasUpdated;

@Entity(name = "relation_term")
@Getter
@Setter
public class TermRelationEntity implements HasDeleted, HasUpdated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private long version;
    @Column
    private Timestamp created;
    @Column
    private Timestamp deleted;
    @Column
    private Timestamp updated;
    @Column
    private long relationId;
    @Column(name = "term1_id")
    private long term1Id;
    @Column(name = "term2_id")
    private long term2Id;
}
