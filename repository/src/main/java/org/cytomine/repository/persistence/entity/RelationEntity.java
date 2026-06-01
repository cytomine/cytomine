package org.cytomine.repository.persistence.entity;

import java.time.LocalDateTime;

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
    private LocalDateTime created;
    @Column
    private LocalDateTime updated;
    @Column
    private LocalDateTime deleted;
    @Column
    private String name;

}
