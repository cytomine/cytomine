package org.cytomine.repository.persistence.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity(name = "relation_term")
@Getter
public class RelationTermEntity {
    @Id
    private long id;
    @Column
    private long version;
    @Column
    private Date created;
    @Column
    private Date deleted;
    @Column
    private String name;
    @Column
    private Date updated;
    @Column
    private long relationId;
    @Column
    private long term1Id;
    @Column
    private long term2Id;
}
