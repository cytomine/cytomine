package org.cytomine.repository.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "relation_term")
@Getter
@Setter
public class TermRelationEntity {
    @Id
    private long id;
    @Column
    private long version;
    @Column
    private LocalDateTime created;
    @Column
    private LocalDateTime deleted;
    @Column
    private String name;
    @Column
    private LocalDateTime updated;
    @Column
    private long relationId;
    @Column(name = "term1_id")
    private long term1Id;
    @Column(name = "term2_id")
    private long term2Id;
    @Column
    private long ontologyId;
}
