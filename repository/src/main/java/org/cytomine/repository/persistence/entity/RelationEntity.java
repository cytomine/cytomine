package org.cytomine.repository.persistence.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity(name = "relation")
@Getter
public class RelationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
